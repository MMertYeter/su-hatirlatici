package com.sutakip.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Kullanıcının mağazadan satın aldığı (puanla talep ettiği) bir ürünün envanter kaydı.
 * urunId, MagazaUrunleri.kt içindeki MagazaUrunu.id ile eşleşir. Ürün mağaza listesinden
 * kaldırılsa bile geçmiş envanter kayıtları (urunAdiSnapshot sayesinde) okunabilir kalır.
 */
@Entity(tableName = "envanter_ogeleri")
data class EnvanterOgesi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val urunId: String,
    val urunAdiSnapshot: String,
    val urunEmojiSnapshot: String,
    val puanMaliyetiSnapshot: Int,
    val satinAlmaTarihiEpochMs: Long,
    val teslimEdildiMi: Boolean = false
)
