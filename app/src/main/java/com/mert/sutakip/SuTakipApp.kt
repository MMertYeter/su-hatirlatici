package com.mert.sutakip

import android.app.Application
import com.mert.sutakip.di.AppContainer
import com.mert.sutakip.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SuTakipApp : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.createChannel(this)

        appScope.launch {
            container.waterRepository.ensureBadgesSeeded()
        }
    }
}
