package com.mert.sutakip.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // WorkManager periyodik işleri cihaz yeniden başladığında otomatik
            // olarak yeniden zamanlar; burada yalnızca ilk kurulumu garanti ediyoruz.
            ReminderScheduler.planlamayiBaslat(context)
        }
    }
}
