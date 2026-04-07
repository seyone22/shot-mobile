package dev.seyone.shot.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.seyone.shot.data.local.entity.ArcherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArcherDao {
    @Query("SELECT * FROM archers ORDER BY name ASC")
    fun getAllArchers(): Flow<List<ArcherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArcher(archer: ArcherEntity)

    @Delete
    suspend fun deleteArcher(archer: ArcherEntity)
}