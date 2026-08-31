package com.sutakip.app.ui.magaza

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sutakip.app.SuTakipApp
import com.sutakip.app.data.local.entity.EnvanterOgesi
import com.sutakip.app.data.store.MagazaUrunleri
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MagazaUiState(
    val toplamPuan: Int = 0,
    val envanter: List<EnvanterOgesi> = emptyList()
)

/** Ürün talep etme sonucu: UI'nin göstereceği kısa geri bildirim için. */
sealed class SatinAlmaSonucu {
    data class Basarili(val urunAdi: String) : SatinAlmaSonucu()
    object YetersizPuan : SatinAlmaSonucu()
}

class MagazaViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SuTakipApp).container
    private val puanRepo = container.puanRepository
    private val envanterDao = container.envanterDao

    val urunler = MagazaUrunleri.urunler

    val uiState: StateFlow<MagazaUiState> = combine(
        puanRepo.toplamPuanFlow,
        envanterDao.observeAll()
    ) { toplamPuan, envanter ->
        MagazaUiState(toplamPuan = toplamPuan, envanter = envanter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MagazaUiState()
    )

    /**
     * Ürünü puanla satın alır (talep eder): yeterli puan varsa puanı düşer ve
     * envantere yeni bir kayıt ekler. callback ile sonucu (başarılı/yetersiz puan)
     * UI'a bildirir.
     */
    fun urunTalepEt(urunId: String, onSonuc: (SatinAlmaSonucu) -> Unit) {
        val urun = MagazaUrunleri.bul(urunId) ?: return
        viewModelScope.launch {
            val basarili = puanRepo.puanHarca(urun.puanMaliyeti)
            if (basarili) {
                envanterDao.insert(
                    EnvanterOgesi(
                        urunId = urun.id,
                        urunAdiSnapshot = urun.ad,
                        urunEmojiSnapshot = urun.emoji,
                        puanMaliyetiSnapshot = urun.puanMaliyeti,
                        satinAlmaTarihiEpochMs = System.currentTimeMillis()
                    )
                )
                onSonuc(SatinAlmaSonucu.Basarili(urun.ad))
            } else {
                onSonuc(SatinAlmaSonucu.YetersizPuan)
            }
        }
    }

    /**
     * Envanterden bir ögeyi kaldırır (kullanıcı hediyeyi gerçekte teslim aldığında).
     * Çağıran taraf (MagazaScreen) şifre kontrolünü zaten yapmış olmalı — bu fonksiyon
     * şifre kontrolü yapmaz, sadece silme işlemini gerçekleştirir.
     */
    fun envantedenKaldir(oge: EnvanterOgesi) {
        viewModelScope.launch {
            envanterDao.delete(oge)
        }
    }
}
