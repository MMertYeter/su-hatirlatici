package com.sutakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bir günün toplam tüketimi ve o günkü hedef anlık görüntüsü.
 * hedefMlSnapshot, geçmiş günlerin istatistiklerinin sonradan
 * değişen profil hedefinden etkilenmemesi için o günkü hedefi saklar.
 */
@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val tarih: String, // "2026-08-26"
    val toplamMl: Int = 0,
    val hedefMlSnapshot: Int
)
