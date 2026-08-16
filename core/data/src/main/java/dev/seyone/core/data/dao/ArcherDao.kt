package dev.seyone.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.seyone.core.data.entity.ArcherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherDao {
    @Query("SELECT * FROM archers ORDER BY name ASC")
    fun getAllArchers(): Flow<List<ArcherEntity>>

    @Query("SELECT * FROM archers ORDER BY name ASC")
    suspend fun getAllArchersSync(): List<ArcherEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArcher(archer: ArcherEntity): Long

    @Delete
    suspend fun deleteArcher(archer: ArcherEntity): Int
}