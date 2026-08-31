package com.mert.sutakip.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mert.sutakip.data.local.dao.BadgeDao
import com.mert.sutakip.data.local.dao.DailyLogDao
import com.mert.sutakip.data.local.dao.EnvanterDao
import com.mert.sutakip.data.local.dao.WaterEntryDao
import com.mert.sutakip.data.local.entity.Badge
import com.mert.sutakip.data.local.entity.DailyLog
import com.mert.sutakip.data.local.entity.EnvanterOgesi
import com.mert.sutakip.data.local.entity.WaterEntry

@Database(
    entities = [WaterEntry::class, DailyLog::class, Badge::class, EnvanterOgesi::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SuTakipDatabase : RoomDatabase() {

    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun badgeDao(): BadgeDao
    abstract fun envanterDao(): EnvanterDao

    companion object {
        @Volatile
        private var INSTANCE: SuTakipDatabase? = null

        fun getInstance(context: Context): SuTakipDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SuTakipDatabase::class.java,
                    "su_takip.db"
                )
                    // v1 -> v2: icecekTuru alanı eklendi (kahve desteği).
                    // v2 -> v3: envanter_ogeleri tablosu eklendi (puan/mağaza sistemi).
                    // Uygulama henüz geniş kullanıcı kitlesine ulaşmadığı için şema
                    // geçişini basit tutuyoruz; eski kullanıcıların günlük kayıtları
                    // sıfırlanır (puan DataStore'da ayrı tutulduğu için etkilenmez).
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
