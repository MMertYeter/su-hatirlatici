package com.sutakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey val id: String,       // "streak_3", "streak_7", "streak_30", "streak_100"
    val ad: String,                   // "3 Günlük Seri"
    val kosulGun: Int,                // gerekli seri gün sayısı
    val kazanildiMi: Boolean = false,
    val kazanmaTarihi: String? = null
)

/** Uygulama ilk açılışta bu sabit rozet setiyle veritabanını doldurur. */
object DefaultBadges {
    val list = listOf(
        Badge(id = "streak_3", ad = "3 Günlük Seri", kosulGun = 3),
        Badge(id = "streak_7", ad = "7 Günlük Seri", kosulGun = 7),
        Badge(id = "streak_30", ad = "30 Günlük Seri", kosulGun = 30),
        Badge(id = "streak_100", ad = "100 Günlük Seri", kosulGun = 100)
    )
}
