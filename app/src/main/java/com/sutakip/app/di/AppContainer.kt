package com.sutakip.app.di

import android.content.Context
import com.sutakip.app.data.datastore.UserPreferencesRepository
import com.sutakip.app.data.local.SuTakipDatabase
import com.sutakip.app.data.repository.WaterRepository
import com.sutakip.app.data.store.PuanRepository

/**
 * Basit, elle yazılmış bir bağımlılık konteyneri (Hilt/Dagger yerine).
 * Küçük/orta ölçekli bir uygulama için yeterli ve build karmaşıklığını azaltır.
 */
class AppContainer(context: Context) {

    private val database = SuTakipDatabase.getInstance(context)

    val userPreferencesRepository = UserPreferencesRepository(context)
    val puanRepository = PuanRepository(context)
    val envanterDao = database.envanterDao()

    val waterRepository = WaterRepository(
        waterEntryDao = database.waterEntryDao(),
        dailyLogDao = database.dailyLogDao(),
        badgeDao = database.badgeDao()
    )
}
