package com.mert.sutakip.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.mert.sutakip.data.local.entity.WaterEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {

    @Insert
    suspend fun insert(entry: WaterEntry): Long

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("SELECT * FROM water_entries WHERE tarih = :tarih ORDER BY tarihSaatEpochMs DESC")
    fun entriesForDay(tarih: String): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries WHERE tarih = :tarih ORDER BY tarihSaatEpochMs DESC LIMIT 1")
    suspend fun lastEntryForDay(tarih: String): WaterEntry?

    @Query("SELECT * FROM water_entries WHERE tarih BETWEEN :startTarih AND :endTarih ORDER BY tarihSaatEpochMs ASC")
    suspend fun entriesInRange(startTarih: String, endTarih: String): List<WaterEntry>

    /** Günün tüm kayıtlarının (ekleme + azaltma) toplamı. Azaltma kayıtları negatif miktarlıdır. */
    @Query("SELECT COALESCE(SUM(miktarMl), 0) FROM water_entries WHERE tarih = :tarih")
    suspend fun toplamForDay(tarih: String): Int
}
