package com.sutakip.app.notification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sutakip.app.SuTakipApp
import com.sutakip.app.util.MotivationMessages
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

private val Context.reminderState by preferencesDataStore(name = "reminder_state")

/**
 * Her 15 dakikada bir çalışan job. Spesifikasyon Bölüm 4'teki iki mekanizmayı
 * (temel dağıtım + adaptif düzeltme) burada birleştirir:
 *
 * 1. Uyku saatleri içindeysek hiçbir şey yapmadan çıkar.
 * 2. Son bildirimden bu yana min. 30 dk geçmediyse çıkar.
 * 3. Güne göre "beklenen" tüketimi (uyanma saatinden bu yana geçen süre
 *    oranında hedefin ne kadarının içilmiş olması gerektiğini) hesaplar.
 * 4. Gerçek tüketimle karşılaştırır:
 *    - geride/tam zamanında -> yaklaşık yarım saatte bir (30dk), "hatırlatıcı" ton
 *    - önde -> biraz daha seyrek (45dk), "tebrik edici" ton
 * 5. Bildirime dokunma su eklemez; sadece uygulamayı açar (bkz. NotificationHelper).
 */
class AdaptiveReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    private object Keys {
        val SON_BILDIRIM_EPOCH_MS = longPreferencesKey("son_bildirim_epoch_ms")
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as SuTakipApp
        val prefsRepo = app.container.userPreferencesRepository
        val waterRepo = app.container.waterRepository

        val profile = prefsRepo.userProfileFlow.first()
        if (!profile.bildirimlerAcik || !profile.onboardingTamamlandi) {
            return Result.success()
        }
        if (!NotificationHelper.bildirimIzniVarMi(applicationContext)) {
            return Result.success()
        }

        val simdi = LocalDateTime.now()
        val simdiDk = simdi.toLocalTime().toSecondOfDay() / 60

        val uyanmaDk = profile.uyanmaSaatiDk
        val uykuDk = profile.uykuSaatiDk

        // Uyku saatleri içindeysek bildirim gönderme.
        if (!uyanikMi(simdiDk, uyanmaDk, uykuDk)) {
            return Result.success()
        }

        // Son bildirimden bu yana geçen süreyi kontrol et (min 45dk kuralı).
        val dataStore = applicationContext.reminderState
        val sonBildirimMs = dataStore.data.first()[Keys.SON_BILDIRIM_EPOCH_MS] ?: 0L
        val gecenDk = (System.currentTimeMillis() - sonBildirimMs) / 60000

        val gunluk = waterRepo.bugununGunlugu().first()
        val icilenMl = gunluk?.toplamMl ?: 0
        val hedefMl = profile.gunlukHedefMl

        // Beklenen tüketim: uyanma saatinden bu yana geçen süre oranı x hedef
        val toplamUyanikDk = max(1, dakikaFarki(uyanmaDk, uykuDk))
        val geceninBasindanGecenDk = max(0, dakikaFarki(uyanmaDk, simdiDk))
        val gunOrani = min(1.0, geceninBasindanGecenDk.toDouble() / toplamUyanikDk.toDouble())
        val beklenenMl = (hedefMl * gunOrani).toInt()

        val fark = beklenenMl - icilenMl
        val farkOrani = if (beklenenMl > 0) fark.toDouble() / beklenenMl.toDouble() else 0.0

        val kalanDk = max(0, dakikaFarki(simdiDk, uykuDk))
        val sonIkiSaatteMi = kalanDk in 0..120

        val gerideKaldiMi = farkOrani >= 0.20
        val gerekliAralikDk = when {
            gerideKaldiMi && sonIkiSaatteMi -> ReminderScheduler.MIN_BILDIRIM_ARALIGI_DK // en sık, min 30dk
            gerideKaldiMi -> ReminderScheduler.MIN_BILDIRIM_ARALIGI_DK // geride iken de yarım saatte bir
            else -> 45L // normal/önde iken biraz daha seyrek
        }

        if (gecenDk < gerekliAralikDk) {
            return Result.success()
        }

        if (icilenMl >= hedefMl) {
            // Hedef zaten tamamlandıysa gün içinde daha fazla hatırlatma gönderme.
            return Result.success()
        }

        val isim = profile.isim.ifBlank { "Şampiyon" }
        val mesaj = if (gerideKaldiMi) {
            MotivationMessages.rastgeleGeriKalma(isim)
        } else {
            MotivationMessages.rastgeleOnde(isim)
        }

        NotificationHelper.bildirimGoster(applicationContext, mesaj)
        dataStore.edit { it[Keys.SON_BILDIRIM_EPOCH_MS] = System.currentTimeMillis() }

        return Result.success()
    }

    private fun uyanikMi(simdiDk: Int, uyanmaDk: Int, uykuDk: Int): Boolean {
        return if (uyanmaDk <= uykuDk) {
            simdiDk in uyanmaDk..uykuDk
        } else {
            // Uyku saati gece yarısını geçiyorsa (örn. uyanma 07:00, uyku 01:00)
            simdiDk >= uyanmaDk || simdiDk <= uykuDk
        }
    }

    /** İki günün-dakikası arasındaki farkı, gece yarısını sarmalayarak hesaplar. */
    private fun dakikaFarki(baslangicDk: Int, bitisDk: Int): Int {
        return if (bitisDk >= baslangicDk) bitisDk - baslangicDk else (1440 - baslangicDk) + bitisDk
    }
}
