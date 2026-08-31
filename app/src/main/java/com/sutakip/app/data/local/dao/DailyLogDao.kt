package com.sutakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sutakip.app.data.local.entity.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(log: DailyLog)

    @Update
    suspend fun update(log: DailyLog)

    @Query("SELECT * FROM daily_logs WHERE tarih = :tarih")
    suspend fun getByDate(tarih: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE tarih = :tarih")
    fun observeByDate(tarih: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE tarih BETWEEN :startTarih AND :endTarih ORDER BY tarih ASC")
    suspend fun getRange(startTarih: String, endTarih: String): List<DailyLog>

    @Query("SELECT * FROM daily_logs ORDER BY tarih DESC")
    suspend fun getAllDescending(): List<DailyLog>
}
