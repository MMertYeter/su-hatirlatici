package com.mert.sutakip.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mert.sutakip.data.local.dao.BadgeDao
import com.mert.sutakip.data.local.dao.DailyLogDao
import com.mert.sutakip.data.local.dao.WaterEntryDao
import com.mert.sutakip.data.local.entity.Badge
import com.mert.sutakip.data.local.entity.DailyLog
import com.mert.sutakip.data.local.entity.WaterEntry

@Database(
    entities = [WaterEntry::class, DailyLog::class, Badge::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SuTakipDatabase : RoomDatabase() {

    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun badgeDao(): BadgeDao

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
                    // v1 -> v2: icecekTuru alanı eklendi (kahve desteği). Uygulama henüz
                    // geniş kullanıcı kitlesine ulaşmadığı için şema geçişini basit tutuyoruz;
                    // v1'den güncelleyen kullanıcıların günlük kayıtları sıfırlanır.
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
