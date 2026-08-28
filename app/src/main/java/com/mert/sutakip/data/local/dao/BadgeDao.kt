package com.mert.sutakip.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mert.sutakip.data.local.entity.Badge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(badges: List<Badge>)

    @Update
    suspend fun update(badge: Badge)

    @Query("SELECT * FROM badges ORDER BY kosulGun ASC")
    fun observeAll(): Flow<List<Badge>>

    @Query("SELECT * FROM badges WHERE kazanildiMi = 0 ORDER BY kosulGun ASC")
    suspend fun getUnearned(): List<Badge>

    @Query("SELECT COUNT(*) FROM badges")
    suspend fun count(): Int
}
