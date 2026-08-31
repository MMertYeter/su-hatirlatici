package com.mert.sutakip.di

import android.content.Context
import com.mert.sutakip.data.datastore.UserPreferencesRepository
import com.mert.sutakip.data.local.SuTakipDatabase
import com.mert.sutakip.data.repository.WaterRepository
import com.mert.sutakip.data.store.PuanRepository

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
