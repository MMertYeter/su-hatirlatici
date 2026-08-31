package com.sutakip.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sutakip.app.data.local.entity.EnvanterOgesi
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvanterDao {

    @Insert
    suspend fun insert(oge: EnvanterOgesi): Long

    @Delete
    suspend fun delete(oge: EnvanterOgesi)

    @Query("SELECT * FROM envanter_ogeleri ORDER BY satinAlmaTarihiEpochMs DESC")
    fun observeAll(): Flow<List<EnvanterOgesi>>
}
