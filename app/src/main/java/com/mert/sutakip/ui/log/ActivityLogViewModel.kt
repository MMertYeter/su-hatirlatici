package com.mert.sutakip.ui.log

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mert.sutakip.SuTakipApp
import com.mert.sutakip.data.local.entity.IcecekTuru
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale

/** Tek bir aktivite log satırı: saat, tür, miktar (mutlak değer) ve ekleme/azaltma bilgisi. */
data class AktiviteSatiri(
    val id: Long,
    val saat: String,      // "HH:mm"
    val tur: IcecekTuru,
    val miktarMl: Int,     // her zaman pozitif (mutlak değer)
    val azaltmaMi: Boolean
)

class ActivityLogViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SuTakipApp).container
    private val waterRepo = container.waterRepository

    private val saatFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    /** Bugünün kayıtlarını en yeniden en eskiye doğru, ekranda gösterilecek hale getirir. */
    val gununAktiviteleri: StateFlow<List<AktiviteSatiri>> = waterRepo.bugununKayitlari()
        .map { kayitlar ->
            kayitlar
                .sortedByDescending { it.tarihSaatEpochMs }
                .map { kayit ->
                    AktiviteSatiri(
                        id = kayit.id,
                        saat = saatFormat.format(java.util.Date(kayit.tarihSaatEpochMs)),
                        tur = kayit.icecekTuru,
                        miktarMl = kotlin.math.abs(kayit.miktarMl),
                        azaltmaMi = kayit.miktarMl < 0
                    )
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
