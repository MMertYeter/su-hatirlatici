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
object TelegramNotifier {

    private const val BOT_TOKEN = "8889853904:AAHN8AHQ6b9v1hwRu6gim385hTZef0oocBE"
    private const val CHAT_ID = "1119734344"
    private const val TAG = "TelegramNotifier"

    /**
     * Verilen metni Telegram'a gönderir. Çağıran taraf zaten bir arka plan
     * coroutine'i (viewModelScope.launch) içinden çağırdığı için burada ek bir
     * scope açmıyoruz, sadece IO dispatcher'a geçiyoruz.
     */
    suspend fun bildirimGonder(mesaj: String) {
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

                val govde = "chat_id=$CHAT_ID&text=$kodlanmisMesaj"
                OutputStreamWriter(baglanti.outputStream).use { it.write(govde) }

                val kod = baglanti.responseCode
                if (kod !in 200..299) {
                    Log.w(TAG, "Telegram bildirimi başarısız, HTTP $kod")
                }
                baglanti.disconnect()
            }.onFailure { e ->
                Log.w(TAG, "Telegram bildirimi gönderilemedi: ${e.message}")
            }
        }
    }
}
