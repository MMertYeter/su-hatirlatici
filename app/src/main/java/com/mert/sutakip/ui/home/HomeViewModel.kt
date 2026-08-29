package com.mert.sutakip.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mert.sutakip.SuTakipApp
import com.mert.sutakip.data.local.entity.IcecekTuru
import com.mert.sutakip.data.local.entity.WaterEntry
import com.mert.sutakip.data.repository.WaterRepository
import com.mert.sutakip.util.MotivationMessages
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Tek bir bardağın ne kadarının su, ne kadarının kahve olduğunu tutar (0f..1f, toplamı doluluğu verir). */
data class BardakDolumu(
    val suOrani: Float = 0f,
    val kahveOrani: Float = 0f
) {
    val toplamDoluluk: Float get() = (suOrani + kahveOrani).coerceIn(0f, 1f)
}

data class HomeUiState(
    val isim: String = "",
    val toplamMl: Int = 0,
    val hedefMl: Int = 2000,
    val sonDolanBardakIndex: Int = -1,
    val motivasyonMesaji: String? = null,
    val kutlamaGoster: Boolean = false,
    val sonIslemGeriAlinabilir: Boolean = false,
    val bardakDolumlari: List<BardakDolumu> = emptyList(),
    // Özel miktar ekranındaki "Bugün X ml su/kahve içtiniz" önizlemesi için,
    // günün su ve kahve toplamları ayrı ayrı (toplamMl = gunlukSuMl + gunlukKahveMl).
    val gunlukSuMl: Int = 0,
    val gunlukKahveMl: Int = 0
)

private data class TransientState(
    val sonDolanBardakIndex: Int = -1,
    val motivasyonMesaji: String? = null,
    val kutlamaGoster: Boolean = false
)

private const val BARDAK_KAPASITESI_ML = 200

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SuTakipApp).container
    private val waterRepo = container.waterRepository
    private val prefsRepo = container.userPreferencesRepository

    private val _transientState = MutableStateFlow(TransientState())

    // Her başarılı ekleme/azaltma/geri alma işleminden sonra true olur; "Geri Al" snackbar'ının
    // görünüp görünmeyeceğini belirler. Tek amacı bu olduğu için tüm işlemler aynı bayrağı kullanır,
    // böylece hangi işlemin en son yapıldığına bakılmaksızın "Geri Al" her zaman tutarlı çalışır.
    private val _sonIslemVarMi = MutableStateFlow(false)
    val sonIslemVarMi: StateFlow<Boolean> = _sonIslemVarMi.asStateFlow()

    val uiState: StateFlow<HomeUiState> = combine(
        prefsRepo.userProfileFlow,
        waterRepo.bugununGunlugu(),
        waterRepo.bugununKayitlari(),
        _transientState,
        _sonIslemVarMi
    ) { profile, gunluk, kayitlar, transient, sonIslemVarMi ->
        HomeUiState(
            isim = profile.isim,
            // toplamMl artık her zaman water_entries tablosunun toplamıyla senkron tutulan
            // DailyLog.toplamMl'den geliyor; bardaklarla (kayitlar) aynı kaynaktan türediği
            // için ikisi asla birbirinden kopmuyor.
            toplamMl = gunluk?.toplamMl ?: 0,
            hedefMl = profile.gunlukHedefMl,
            sonDolanBardakIndex = transient.sonDolanBardakIndex,
            motivasyonMesaji = transient.motivasyonMesaji,
            kutlamaGoster = transient.kutlamaGoster,
            sonIslemGeriAlinabilir = sonIslemVarMi,
            bardakDolumlari = bardakDolumlariniHesapla(kayitlar, profile.gunlukHedefMl),
            gunlukSuMl = kayitlar.filter { it.icecekTuru == IcecekTuru.SU }.sumOf { it.miktarMl }.coerceAtLeast(0),
            gunlukKahveMl = kayitlar.filter { it.icecekTuru == IcecekTuru.KAHVE }.sumOf { it.miktarMl }.coerceAtLeast(0)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun suEkle(miktarMl: Int) {
        ekle(miktarMl, IcecekTuru.SU)
    }

    /** Kahve de sıvı alımına katkı sağladığı için su ile aynı şekilde toplama eklenir. */
    fun kahveEkle(miktarMl: Int) {
        ekle(miktarMl, IcecekTuru.KAHVE)
    }

    private fun ekle(miktarMl: Int, tur: IcecekTuru) {
        if (miktarMl <= 0) return
        viewModelScope.launch {
            val hedefMl = uiState.value.hedefMl
            val sonuc = waterRepo.suEkle(miktarMl, hedefMl, tur)

            val sonDolanIndex = (sonuc.yeniToplamMl - 1) / BARDAK_KAPASITESI_ML

            val isim = uiState.value.isim.ifBlank { "Şampiyon" }
            val mesaj = if (sonuc.hedefTamamlandiMi) {
                MotivationMessages.rastgeleKutlama(isim)
            } else {
                MotivationMessages.rastgeleNormal(isim)
            }

            _transientState.value = TransientState(sonDolanIndex, mesaj, sonuc.hedefTamamlandiMi)
            _sonIslemVarMi.value = true
        }
    }

    /**
     * Kullanıcı yanlışlıkla fazla su/kahve eklediyse günün toplamını manuel azaltır.
     * Bardak görünümüyle senkron kalması için ayrı, negatif miktarlı bir kayıt olarak
     * saklanır (bkz. WaterRepository.suAzalt) — hiçbir zaman sadece üstteki toplamı
     * değiştirip bardakları olduğu gibi bırakmaz.
     */
    fun suAzalt(miktarMl: Int, tur: IcecekTuru = IcecekTuru.SU) {
        if (miktarMl <= 0) return
        viewModelScope.launch {
            waterRepo.suAzalt(miktarMl, tur)
            // Azaltma bardak dolum animasyonunu tetiklemez (dolma efekti sadece eklemede anlamlıdır),
            // ama kullanıcı işlemi geri alabilsin diye kısa bir onay mesajı gösterilir.
            _transientState.value = TransientState(motivasyonMesaji = "$miktarMl ml azaltıldı")
            _sonIslemVarMi.value = true
        }
    }

    /** En son yapılan işlemi (ekleme, kahve ekleme ya da azaltma fark etmeksizin) geri alır. */
    fun geriAl() {
        viewModelScope.launch {
            waterRepo.sonEklemeyiGeriAl()
            _sonIslemVarMi.value = false
            _transientState.value = TransientState()
        }
    }

    /**
     * Günün kayıtlarını kronolojik sırayla (en eskiden en yeniye) bardaklara dağıtır.
     *
     * SADELEŞTİRİLMİŞ MANTIK: Her bardak SADECE TEK BİR TÜRDEN oluşur — asla aynı
     * bardakta hem su hem kahve olmaz. Bir bardak bir türle doldurulmaya başladıysa,
     * o bardağın kalan kapasitesi sadece o türle doldurulabilir; kapasite dolunca
     * bir sonraki (boş) bardağa geçilir. Böylece "hangi bardağın ne kadarı su ne
     * kadarı kahve" sorusu hiç ortaya çıkmaz, karışıklık tamamen ortadan kalkar.
     *
     * Azaltma (Su Azalt / Kahve Azalt) de aynı basitliktedir: sadece kendi türünden
     * dolu olan en son bardaktan başlayarak, o bardağı boşaltarak geriye doğru iner.
     */
    private fun bardakDolumlariniHesapla(kayitlar: List<WaterEntry>, hedefMl: Int): List<BardakDolumu> {
        val bardakSayisi = ((hedefMl + BARDAK_KAPASITESI_ML - 1) / BARDAK_KAPASITESI_ML).coerceAtLeast(1)
        val dolumlar = MutableList(bardakSayisi) { BardakDolumu() }

        // entriesForDay DESC sırayla geldiği için kronolojik (ASC) sıraya çeviriyoruz.
        val kronolojik = kayitlar.sortedBy { it.tarihSaatEpochMs }

        // Her bardağın hangi türle "kilitlendiğini" tutar (o bardağa ilk hangi tür
        // eklendiyse). null = bardak henüz hiç dolmamış, herhangi bir tür açabilir.
        // dolumlar başlangıçta tamamen boş oluşturulduğu için hepsi null ile başlar.
        val bardakTuru = arrayOfNulls<IcecekTuru>(bardakSayisi)

        for (kayit in kronolojik) {
            if (kayit.miktarMl >= 0) {
                doldur(dolumlar, bardakTuru, kayit.miktarMl, kayit.icecekTuru)
            } else {
                azalt(dolumlar, bardakTuru, -kayit.miktarMl, kayit.icecekTuru)
            }
        }

        return dolumlar
    }

    /**
     * miktarMl kadar tur sıvısını, ilk boş ya da zaten aynı türle kısmen dolu olan
     * bardaktan başlayarak sırayla doldurur. Farklı türle dolu/kısmen dolu bir bardağa
     * asla dokunmaz — o bardak dolu sayılır, bir sonrakine geçilir.
     */
    private fun doldur(
        dolumlar: MutableList<BardakDolumu>,
        bardakTuru: Array<IcecekTuru?>,
        miktarMl: Int,
        tur: IcecekTuru
    ) {
        var kalanMiktar = miktarMl
        var bardakIndex = 0
        while (kalanMiktar > 0 && bardakIndex < dolumlar.size) {
            val bulunanTur = bardakTuru[bardakIndex]
            if (bulunanTur != null && bulunanTur != tur) {
                // Bu bardak zaten diğer türle meşgul, dokunmadan geç.
                bardakIndex++
                continue
            }

            val mevcut = dolumlar[bardakIndex]
            val doluOran = if (tur == IcecekTuru.KAHVE) mevcut.kahveOrani else mevcut.suOrani
            val kalanKapasiteMl = ((1f - doluOran) * BARDAK_KAPASITESI_ML).let { kotlin.math.round(it).toInt() }

            if (kalanKapasiteMl <= 0) {
                bardakIndex++
                continue
            }

            val buAdimdaEklenen = minOf(kalanMiktar, kalanKapasiteMl)
            val eklenenOran = buAdimdaEklenen.toFloat() / BARDAK_KAPASITESI_ML

            dolumlar[bardakIndex] = if (tur == IcecekTuru.KAHVE) {
                mevcut.copy(kahveOrani = (mevcut.kahveOrani + eklenenOran).coerceAtMost(1f))
            } else {
                mevcut.copy(suOrani = (mevcut.suOrani + eklenenOran).coerceAtMost(1f))
            }
            bardakTuru[bardakIndex] = tur

            kalanMiktar -= buAdimdaEklenen
            if (buAdimdaEklenen >= kalanKapasiteMl) bardakIndex++
        }
    }

    /**
     * miktarMl kadar tur sıvısını, o türle dolu EN SON bardaktan başlayarak geriye
     * doğru boşaltır. Diğer türe hiçbir zaman dokunmaz.
     */
    private fun azalt(
        dolumlar: MutableList<BardakDolumu>,
        bardakTuru: Array<IcecekTuru?>,
        miktarMl: Int,
        tur: IcecekTuru
    ) {
        var kalanAzaltma = miktarMl
        var bardakIndex = dolumlar.size - 1
        while (kalanAzaltma > 0 && bardakIndex >= 0) {
            if (bardakTuru[bardakIndex] == tur) {
                val mevcut = dolumlar[bardakIndex]
                val doluOran = if (tur == IcecekTuru.KAHVE) mevcut.kahveOrani else mevcut.suOrani
                val doluMl = kotlin.math.round(doluOran * BARDAK_KAPASITESI_ML).toInt()

                if (doluMl > 0) {
                    val buAdimdaAzaltilan = minOf(kalanAzaltma, doluMl)
                    val azaltilanOran = buAdimdaAzaltilan.toFloat() / BARDAK_KAPASITESI_ML

                    dolumlar[bardakIndex] = mevcut.uygulaAzaltma(tur, azaltilanOran)
                    if (dolumlar[bardakIndex].toplamDoluluk <= 0f) bardakTuru[bardakIndex] = null

                    kalanAzaltma -= buAdimdaAzaltilan
                }
            }
            bardakIndex--
        }
    }

    private fun BardakDolumu.uygulaAzaltma(tur: IcecekTuru, miktar: Float): BardakDolumu =
        if (tur == IcecekTuru.KAHVE) {
            copy(kahveOrani = (kahveOrani - miktar).coerceAtLeast(0f))
        } else {
            copy(suOrani = (suOrani - miktar).coerceAtLeast(0f))
        }

    fun mesajGosterildi() {
        _transientState.value = _transientState.value.copy(motivasyonMesaji = null, kutlamaGoster = false)
    }
}
