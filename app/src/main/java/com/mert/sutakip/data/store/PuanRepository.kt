package com.mert.sutakip.data.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.puanDataStore by preferencesDataStore(name = "puan_prefs")

/** 100ml sıvı (su ya da kahve) başına kazanılan puan. */
const val PUAN_PER_100ML = 10

/** Günlük hedef ilk kez tamamlandığında kazanılan bonus puan. */
const val HEDEF_TAMAMLAMA_BONUS_PUANI = 30

/**
 * Kullanıcının toplam puanını (harcanmamış bakiye) DataStore üzerinden tutar. Puan
 * kazanımı/harcaması tamamen cihaz içinde, tek kullanıcılıktır (aile/arkadaş grubu
 * senaryosunda her kişinin kendi telefonunda kendi puanı birikir).
 *
 * gunHedefBonusuVerilenGunler: bugüne kadar hedef bonusunun verildiği tarihlerin seti
 * ("yyyy-MM-dd"), aynı gün içinde art arda azaltma/ekleme yapılıp hedefin tekrar tekrar
 * "yeni tamamlandı" sayılmasını (ve bonusun tekrar tekrar verilmesini) önlemek için.
 */
class PuanRepository(private val context: Context) {

    private object Keys {
        val TOPLAM_PUAN = intPreferencesKey("toplam_puan")
        val BONUS_VERILEN_GUNLER = stringSetPreferencesKey("bonus_verilen_gunler")
    }

    val toplamPuanFlow: Flow<Int> = context.puanDataStore.data.map { prefs ->
        prefs[Keys.TOPLAM_PUAN] ?: 0
    }

    suspend fun puanEkle(puan: Int) {
        if (puan == 0) return
        context.puanDataStore.edit { prefs ->
            val mevcut = prefs[Keys.TOPLAM_PUAN] ?: 0
            prefs[Keys.TOPLAM_PUAN] = (mevcut + puan).coerceAtLeast(0)
        }
    }

    /** Bugün için hedef bonusu daha önce verildiyse true döner (tekrar vermemek için). */
    suspend fun bugunBonusVerildiMi(tarih: String): Boolean {
        val prefs = context.puanDataStore.data.first()
        return prefs[Keys.BONUS_VERILEN_GUNLER]?.contains(tarih) == true
    }

    suspend fun bugunBonusVerildiOlarakIsaretle(tarih: String) {
        context.puanDataStore.edit { prefs ->
            val mevcutSet = prefs[Keys.BONUS_VERILEN_GUNLER] ?: emptySet()
            prefs[Keys.BONUS_VERILEN_GUNLER] = mevcutSet + tarih
        }
    }

    /** Ürün satın alındığında (talep edildiğinde) puanı düşer. Yetersizse false döner. */
    suspend fun puanHarca(puan: Int): Boolean {
        var basarili = false
        context.puanDataStore.edit { prefs ->
            val mevcut = prefs[Keys.TOPLAM_PUAN] ?: 0
            if (mevcut >= puan) {
                prefs[Keys.TOPLAM_PUAN] = mevcut - puan
                basarili = true
            }
        }
        return basarili
    }
}
