package com.sutakip.app.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sutakip.app.SuTakipApp
import com.sutakip.app.data.local.entity.Badge
import com.sutakip.app.data.local.entity.DailyLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class StatsGorunum { HAFTALIK, AYLIK }

data class GunlukVeriNoktasi(
    val tarih: LocalDate,
    val toplamMl: Int,
    val hedefMl: Int,
    val etiket: String
)

data class StatsUiState(
    val gorunum: StatsGorunum = StatsGorunum.HAFTALIK,
    val gunler: List<GunlukVeriNoktasi> = emptyList(),
    val gunlukOrtalamaMl: Int = 0,
    val enIyiGunMl: Int = 0,
    val hedefTutturmaOrani: Int = 0, // yüzde
    val mevcutSeri: Int = 0,
    val rozetler: List<Badge> = emptyList()
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SuTakipApp).container
    private val waterRepo = container.waterRepository

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        yukle(StatsGorunum.HAFTALIK)
        viewModelScope.launch {
            waterRepo.rozetler().collectLatest { rozetler ->
                _state.value = _state.value.copy(rozetler = rozetler)
            }
        }
    }

    fun gorunumDegistir(gorunum: StatsGorunum) {
        yukle(gorunum)
    }

    private fun yukle(gorunum: StatsGorunum) {
        viewModelScope.launch {
            val bugun = LocalDate.now()
            val gunSayisi = if (gorunum == StatsGorunum.HAFTALIK) 7 else 30
            val baslangic = bugun.minusDays((gunSayisi - 1).toLong())

            val kayitlar = waterRepo.gunlukleriGetir(baslangic, bugun)
            val kayitMap = kayitlar.associateBy { it.tarih }

            val etiketFmt = DateTimeFormatter.ofPattern(if (gorunum == StatsGorunum.HAFTALIK) "EEE" else "d")
            val gunler = (0 until gunSayisi).map { offset ->
                val tarih = baslangic.plusDays(offset.toLong())
                val kayit = kayitMap[tarih.format(DateTimeFormatter.ISO_LOCAL_DATE)]
                GunlukVeriNoktasi(
                    tarih = tarih,
                    toplamMl = kayit?.toplamMl ?: 0,
                    hedefMl = kayit?.hedefMlSnapshot ?: 0,
                    etiket = tarih.format(etiketFmt)
                )
            }

            val gunlerVeriIle = gunler.filter { it.toplamMl > 0 || it.hedefMl > 0 }
            val ortalama = if (gunlerVeriIle.isNotEmpty()) gunlerVeriIle.sumOf { it.toplamMl } / gunlerVeriIle.size else 0
            val enIyi = gunler.maxOfOrNull { it.toplamMl } ?: 0
            val tutturulanGun = gunlerVeriIle.count { it.toplamMl >= it.hedefMl && it.hedefMl > 0 }
            val oran = if (gunlerVeriIle.isNotEmpty()) (tutturulanGun * 100) / gunlerVeriIle.size else 0

            val seri = waterRepo.mevcutSeri()

            _state.value = _state.value.copy(
                gorunum = gorunum,
                gunler = gunler,
                gunlukOrtalamaMl = ortalama,
                enIyiGunMl = enIyi,
                hedefTutturmaOrani = oran,
                mevcutSeri = seri
            )
        }
    }
}
