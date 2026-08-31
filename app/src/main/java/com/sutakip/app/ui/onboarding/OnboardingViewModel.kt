package com.sutakip.app.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sutakip.app.SuTakipApp
import com.sutakip.app.data.datastore.Cinsiyet
import com.sutakip.app.util.HedefHesaplayici
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingState(
    val adim: Int = 0, // 0: isim, 1: fiziksel, 2: uyku/uyanma, 3: hedef
    val isim: String = "",
    val boyCm: Float = 170f,
    val kiloKg: Float = 70f,
    val yas: Int = 25,
    val cinsiyet: Cinsiyet = Cinsiyet.BELIRTILMEDI,
    val uyanmaSaatiDk: Int = 7 * 60,
    val uykuSaatiDk: Int = 23 * 60,
    val hesaplananHedefMl: Int = 2000,
    val kullaniciHedefMl: Int? = null, // kullanıcı elle değiştirdiyse
    val tamamlandi: Boolean = false
) {
    val etkinHedefMl: Int get() = kullaniciHedefMl ?: hesaplananHedefMl
}

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = (application as SuTakipApp).container.userPreferencesRepository

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun isimGuncelle(isim: String) {
        _state.value = _state.value.copy(isim = isim)
    }

    fun fizikselGuncelle(boyCm: Float, kiloKg: Float, yas: Int, cinsiyet: Cinsiyet) {
        _state.value = _state.value.copy(boyCm = boyCm, kiloKg = kiloKg, yas = yas, cinsiyet = cinsiyet)
    }

    fun uykuUyanmaGuncelle(uyanmaDk: Int, uykuDk: Int) {
        _state.value = _state.value.copy(uyanmaSaatiDk = uyanmaDk, uykuSaatiDk = uykuDk)
        hedefiYenidenHesapla()
    }

    fun hedefiYenidenHesapla() {
        val s = _state.value
        val hesaplanan = HedefHesaplayici.hesapla(s.boyCm, s.kiloKg, s.yas, s.cinsiyet)
        _state.value = s.copy(hesaplananHedefMl = hesaplanan)
    }

    fun hedefiElleAyarla(yeniHedefMl: Int) {
        _state.value = _state.value.copy(kullaniciHedefMl = yeniHedefMl.coerceIn(500, 6000))
    }

    fun ileriGit() {
        val s = _state.value
        if (s.adim == 2) {
            hedefiYenidenHesapla()
        }
        _state.value = s.copy(adim = (s.adim + 1).coerceAtMost(3))
    }

    fun geriGit() {
        _state.value = _state.value.copy(adim = (_state.value.adim - 1).coerceAtLeast(0))
    }

    fun tamamla(onDone: () -> Unit) {
        viewModelScope.launch {
            val s = _state.value
            prefsRepo.updateIsim(s.isim)
            prefsRepo.updateFizikselBilgiler(s.boyCm, s.kiloKg, s.yas, s.cinsiyet)
            prefsRepo.updateUykuUyanma(s.uyanmaSaatiDk, s.uykuSaatiDk)
            prefsRepo.updateHedef(s.etkinHedefMl, manuel = s.kullaniciHedefMl != null)
            prefsRepo.setOnboardingTamamlandi()
            onDone()
        }
    }
}
