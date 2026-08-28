package com.mert.sutakip.data.repository

import com.mert.sutakip.data.local.dao.BadgeDao
import com.mert.sutakip.data.local.dao.DailyLogDao
import com.mert.sutakip.data.local.dao.WaterEntryDao
import com.mert.sutakip.data.local.entity.Badge
import com.mert.sutakip.data.local.entity.DailyLog
import com.mert.sutakip.data.local.entity.DefaultBadges
import com.mert.sutakip.data.local.entity.IcecekTuru
import com.mert.sutakip.data.local.entity.WaterEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

data class EklemeSonucu(
    val yeniToplamMl: Int,
    val hedefTamamlandiMi: Boolean,
    val eklenenEntryId: Long
)

/**
 * Su/kahve takibinin tek gerçek kaynağı (single source of truth) water_entries tablosudur.
 * Her ekleme pozitif, her azaltma negatif miktarlı bir WaterEntry kaydı olarak saklanır.
 * DailyLog.toplamMl bu kayıtların toplamından türetilir ve her işlemden sonra senkron
 * tutulur; böylece "geri al" her zaman gerçek son işlemi (ekleme ya da azaltma fark
 * etmeksizin) geri alabilir ve bardak görünümü (WaterEntry listesi) ile üstteki toplam
 * asla birbirinden kopmaz.
 */
class WaterRepository(
    private val waterEntryDao: WaterEntryDao,
    private val dailyLogDao: DailyLogDao,
    private val badgeDao: BadgeDao
) {

    suspend fun ensureBadgesSeeded() {
        if (badgeDao.count() == 0) {
            badgeDao.insertAll(DefaultBadges.list)
        }
    }

    fun bugununGunlugu(): Flow<DailyLog?> {
        val bugun = LocalDate.now().format(DATE_FMT)
        return dailyLogDao.observeByDate(bugun)
    }

    fun bugununKayitlari(): Flow<List<WaterEntry>> {
        val bugun = LocalDate.now().format(DATE_FMT)
        return waterEntryDao.entriesForDay(bugun)
    }

    /** Su/kahve ekler, günün DailyLog'unu günceller/oluşturur, hedefe ulaşılıp ulaşılmadığını döner. */
    suspend fun suEkle(miktarMl: Int, hedefMl: Int, icecekTuru: IcecekTuru = IcecekTuru.SU): EklemeSonucu {
        val bugun = LocalDate.now().format(DATE_FMT)
        val simdi = System.currentTimeMillis()

        dailyLogDao.insertIfAbsent(DailyLog(tarih = bugun, toplamMl = 0, hedefMlSnapshot = hedefMl))
        val onceki = dailyLogDao.getByDate(bugun)!!

        val entryId = waterEntryDao.insert(
            WaterEntry(tarih = bugun, tarihSaatEpochMs = simdi, miktarMl = miktarMl, icecekTuru = icecekTuru)
        )

        val yeniToplam = senkronizeGunToplami(bugun)
        val hedefTamamlandiMi = onceki.toplamMl < onceki.hedefMlSnapshot && yeniToplam >= onceki.hedefMlSnapshot

        if (hedefTamamlandiMi) {
            guncelleRozetler()
        }

        return EklemeSonucu(yeniToplam, hedefTamamlandiMi, entryId)
    }

    /**
     * Son işlemi geri alır: en son eklenen WaterEntry kaydını (ekleme ya da azaltma,
     * hangisiyse) siler ve günün toplamını yeniden senkronize eder. Bu sayede "Geri Al"
     * her zaman kullanıcının az önce yaptığı gerçek işlemi geri alır, karışıklık olmaz.
     */
    suspend fun sonEklemeyiGeriAl() {
        val bugun = LocalDate.now().format(DATE_FMT)
        val son = waterEntryDao.lastEntryForDay(bugun) ?: return
        waterEntryDao.delete(son)
        senkronizeGunToplami(bugun)
    }

    /**
     * Günün toplamını belirtilen miktar kadar azaltır (yanlışlıkla fazla girilen suyu/kahveyi
     * düzeltmek için). Ayrı, negatif miktarlı bir WaterEntry kaydı oluşturur; böylece hem
     * "Geri Al" bu azaltmayı da geri alabilir, hem de bardak görünümü anında tutarlı kalır.
     * Günün toplamı 0'ın altına inmeyecek şekilde sınırlanır.
     */
    suspend fun suAzalt(miktarMl: Int, icecekTuru: IcecekTuru = IcecekTuru.SU): Int {
        val bugun = LocalDate.now().format(DATE_FMT)
        val mevcutToplam = waterEntryDao.toplamForDay(bugun)
        if (mevcutToplam <= 0 || miktarMl <= 0) return mevcutToplam.coerceAtLeast(0)

        val gercekAzaltma = minOf(miktarMl, mevcutToplam)
        val simdi = System.currentTimeMillis()

        waterEntryDao.insert(
            WaterEntry(
                tarih = bugun,
                tarihSaatEpochMs = simdi,
                miktarMl = -gercekAzaltma,
                icecekTuru = icecekTuru
            )
        )

        return senkronizeGunToplami(bugun)
    }

    /**
     * water_entries tablosundaki toplamı DailyLog.toplamMl alanına yazar (gerekirse
     * DailyLog'u oluşturur). Bu, iki tablonun her zaman senkron kalmasını garanti eder.
     */
    private suspend fun senkronizeGunToplami(tarih: String): Int {
        val gercekToplam = waterEntryDao.toplamForDay(tarih).coerceAtLeast(0)
        val mevcutLog = dailyLogDao.getByDate(tarih)
        if (mevcutLog != null) {
            dailyLogDao.update(mevcutLog.copy(toplamMl = gercekToplam))
        }
        return gercekToplam
    }

    fun rozetler(): Flow<List<Badge>> = badgeDao.observeAll()

    /** Geçmiş günlerin hedef tutturma durumuna göre serinin uzunluğunu hesaplar ve rozetleri günceller. */
    private suspend fun guncelleRozetler() {
        val gecmis = dailyLogDao.getAllDescending()
        var streak = 0
        for (log in gecmis) {
            if (log.toplamMl >= log.hedefMlSnapshot) streak++ else break
        }

        val bugun = LocalDate.now().format(DATE_FMT)
        val unearned = badgeDao.getUnearned()
        for (badge in unearned) {
            if (streak >= badge.kosulGun) {
                badgeDao.update(badge.copy(kazanildiMi = true, kazanmaTarihi = bugun))
            }
        }
    }

    /** Haftalık/aylık istatistik için tarih aralığındaki günlükleri döner. */
    suspend fun gunlukleriGetir(baslangic: LocalDate, bitis: LocalDate): List<DailyLog> {
        return dailyLogDao.getRange(baslangic.format(DATE_FMT), bitis.format(DATE_FMT))
    }

    /** Mevcut seri (art arda hedefi tutturulan gün sayısı). */
    suspend fun mevcutSeri(): Int {
        val gecmis = dailyLogDao.getAllDescending()
        var streak = 0
        for (log in gecmis) {
            if (log.toplamMl >= log.hedefMlSnapshot) streak++ else break
        }
        return streak
    }
}
