package com.sutakip.app.notification

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Kullanıcı günlük hedefini tamamladığında Mert'in Telegram'ına bildirim gönderir.
 * Bu tamamen opsiyonel, "en iyi çaba" (best-effort) bir özelliktir: internet yoksa,
 * Telegram API'sine ulaşılamıyorsa ya da başka bir hata olursa sessizce başarısız
 * olur — uygulamanın asıl işlevini (su takibi) hiçbir şekilde etkilemez, kullanıcıya
 * hata göstermez.
 *
 * Token ve chat ID burada sabit olarak tutulur (MotivationMessages.kt'deki gibi,
 * GitHub üzerinden kaynak kodda düzenlenir). Bu bot sadece mesaj gönderme yetkisine
 * sahip olduğu için düz metin tutulması düşük risklidir.
 */
/**
 * Telegram'a iki farklı hedefe mesaj gönderir:
 * 1. Sihirbaz'ın kişisel sohbeti: sadece "hedef tamamlandı" başarı bildirimi.
 * 2. SuTakipLog grubu: her su/kahve ekleme-azaltma işleminde, isim + o anki durum
 *    (kaç ml su, kaç ml kahve, hedef ne kadar) — bir log akışı gibi.
 *
 * Bu tamamen opsiyonel, "en iyi çaba" (best-effort) bir özelliktir: internet yoksa,
 * Telegram API'sine ulaşılamıyorsa ya da başka bir hata olursa sessizce başarısız
 * olur — uygulamanın asıl işlevini (su takibi) hiçbir şekilde etkilemez, kullanıcıya
 * hata göstermez.
 *
 * Token ve chat ID'ler burada sabit olarak tutulur (MotivationMessages.kt'deki gibi,
 * GitHub üzerinden kaynak kodda düzenlenir). Bu bot sadece mesaj gönderme yetkisine
 * sahip olduğu için düz metin tutulması düşük risklidir.
 */
object TelegramNotifier {

    private const val BOT_TOKEN = "8671506422:AAGGKSvnOA-W2yv9GkLgU18bNKzFtFI4KhU"
    private const val KISISEL_CHAT_ID = "8903332029"   // Sihirbaz (zafer bildirimleri)
    private const val GRUP_CHAT_ID = "-1003760989523"   // SuTakipLog grubu (loglar)
    private const val TAG = "TelegramNotifier"

    /** Hedef tamamlama başarı bildirimi — sadece Sihirbaz'ın kişisel sohbetine gider. */
    suspend fun basariBildirimiGonder(mesaj: String) {
        gonder(KISISEL_CHAT_ID, mesaj)
    }

    /**
     * Bir su/kahve ekleme ya da azaltma işlemi olduğunda, kimin ne yaptığını ve o
     * anki durumunu grup sohbetine yazar. İsim yoksa (henüz onboarding'de isim
     * girilmediyse) "Biri" olarak gönderilir.
     */
    suspend fun logGonder(mesaj: String) {
        gonder(GRUP_CHAT_ID, mesaj)
    }

    /**
     * Verilen metni belirtilen chat ID'ye gönderir. Çağıran taraf zaten bir arka
     * plan coroutine'i (viewModelScope.launch) içinden çağırdığı için burada ek
     * bir scope açmıyoruz, sadece IO dispatcher'a geçiyoruz.
     */
    private suspend fun gonder(chatId: String, mesaj: String) {
        if (BOT_TOKEN.isBlank() || BOT_TOKEN == "TELEGRAM_BOT_TOKEN_BURAYA") return

        withContext(Dispatchers.IO) {
            runCatching {
                val kodlanmisMesaj = URLEncoder.encode(mesaj, "UTF-8")
                val url = URL("https://api.telegram.org/bot$BOT_TOKEN/sendMessage")
                val baglanti = url.openConnection() as HttpURLConnection
                baglanti.requestMethod = "POST"
                baglanti.doOutput = true
                baglanti.connectTimeout = 8000
                baglanti.readTimeout = 8000
                baglanti.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val govde = "chat_id=$chatId&text=$kodlanmisMesaj"
                OutputStreamWriter(baglanti.outputStream).use { it.write(govde) }

                val kod = baglanti.responseCode
                if (kod !in 200..299) {
                    Log.w(TAG, "Telegram bildirimi başarısız, HTTP $kod (chat: $chatId)")
                }
                baglanti.disconnect()
            }.onFailure { e ->
                Log.w(TAG, "Telegram bildirimi gönderilemedi: ${e.message}")
            }
        }
    }
}
