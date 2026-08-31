package com.sutakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Eklenen sıvının türü. Bardak dolumunda renk ayrımı için kullanılır. */
enum class IcecekTuru {
    SU, KAHVE
}

/**
 * Tek bir su ekleme kaydı. tarih alanı "yyyy-MM-dd" formatında,
 * gunun DailyLog toplamıyla eşleşmesi için ayrı tutulur.
 */
@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tarih: String,          // "2026-08-26"
    val tarihSaatEpochMs: Long, // ekleme anı, undo ve zaman çizelgesi için
    val miktarMl: Int,
    val icecekTuru: IcecekTuru = IcecekTuru.SU
)
