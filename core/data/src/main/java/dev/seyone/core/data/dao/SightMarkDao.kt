package dev.seyone.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.seyone.core.data.entity.SightMarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SightMarkDao {

    @Query("SELECT * FROM sight_marks WHERE bowProfileId = :bowId ORDER BY distanceValue ASC")
    fun getSightMarksForBowStream(bowId: Long): Flow<List<SightMarkEntity>>

    @Query("SELECT * FROM sight_marks WHERE bowProfileId = :bowId AND (arrowSetId = :arrowId OR arrowSetId IS NULL) ORDER BY distanceValue ASC")
    fun getSightMarksForBowAndArrowStream(bowId: Long, arrowId: Long): Flow<List<SightMarkEntity>>

    @Query("SELECT * FROM sight_marks ORDER BY bowProfileId ASC, distanceValue ASC")
    fun getAllSightMarksSync(): List<SightMarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSightMark(sightMark: SightMarkEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSightMarks(sightMarks: List<SightMarkEntity>)

    @Delete
    suspend fun deleteSightMark(sightMark: SightMarkEntity)

    @Query("DELETE FROM sight_marks WHERE bowProfileId = :bowId")
    suspend fun deleteSightMarksForBow(bowId: Long)

    @Query("DELETE FROM sight_marks")
    suspend fun deleteAllSightMarks()
}
