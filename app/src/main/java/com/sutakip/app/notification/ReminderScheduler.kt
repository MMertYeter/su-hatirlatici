package com.sutakip.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Bildirim sistemi Bölüm 4 spesifikasyonuna göre iki parçadan oluşur:
 *
 * A) Temel dağıtım: Uyanma-uyku aralığı ve hedef bardak sayısına göre
 *    WorkManager ile periyodik (saatlik) bir kontrol job'u kurulur; bu job
 *    her çalıştığında "şu an bildirim zamanı mı" diye AdaptiveReminderWorker
 *    içinde karar verir (bkz. AdaptiveReminderWorker).
 *
 * B) Adaptif düzeltme: Aynı periyodik job, beklenen/gerçek tüketimi
 *    karşılaştırarak bir sonraki bildirimin zamanlamasını ve tonunu ayarlar.
 *
 * Not: WorkManager'ın minimum periyodik aralığı 15 dakikadır; bu yüzden
 * "planlama" işini 15 dakikalık bir job ile yapıp, her çalıştığında son
 * bildirimden bu yana min. 30 dakika geçip geçmediğini ve uyku saatleri
 * içinde olup olmadığımızı AdaptiveReminderWorker içinde kontrol ediyoruz.
 * Bu, exact alarm karmaşıklığı olmadan yaklaşık yarım saatte bir bildirim
 * davranışını sağlar.
 */
object ReminderScheduler {

    private const val WORK_NAME = "adaptif_su_hatirlatma"
    const val MIN_BILDIRIM_ARALIGI_DK = 45L

    fun planlamayiBaslat(context: Context) {
        val request = PeriodicWorkRequestBuilder<AdaptiveReminderWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun planlamayiDurdur(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
