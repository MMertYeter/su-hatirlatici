package com.sutakip.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sutakip.app.SuTakipApp
import com.sutakip.app.data.datastore.Cinsiyet
import com.sutakip.app.data.datastore.TemaModu
import com.sutakip.app.data.datastore.UserProfile
import com.sutakip.app.util.HedefHesaplayici
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = (application as SuTakipApp).container.userPreferencesRepository

    val profile: StateFlow<UserProfile> = prefsRepo.userProfileFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfile()
    )

    /** Profil bilgisi değişince hedefi yeniden hesaplar (kullanıcı onayı beklenmeden, override edilmemişse). */
    fun profilGuncelle(
        isim: String,
        boyCm: Float,
        kiloKg: Float,
        yas: Int,
        cinsiyet: Cinsiyet,
        uyanmaDk: Int,
        uykuDk: Int,
        hedefiYenidenHesapla: Boolean
    ) {
        viewModelScope.launch {
            prefsRepo.updateIsim(isim)
            prefsRepo.updateFizikselBilgiler(boyCm, kiloKg, yas, cinsiyet)
            prefsRepo.updateUykuUyanma(uyanmaDk, uykuDk)

            if (hedefiYenidenHesapla) {
                val yeniHedef = HedefHesaplayici.hesapla(boyCm, kiloKg, yas, cinsiyet)
                prefsRepo.updateHedef(yeniHedef, manuel = false)
            }
        }
    }

    fun hedefiManuelAyarla(hedefMl: Int) {
        viewModelScope.launch {
            prefsRepo.updateHedef(hedefMl, manuel = true)
        }
    }

    fun bildirimlerAcikDegistir(acik: Boolean) {
        viewModelScope.launch { prefsRepo.setBildirimlerAcik(acik) }
    }

    fun temaModuDegistir(mod: TemaModu) {
        viewModelScope.launch { prefsRepo.setTemaModu(mod) }
    }
}
