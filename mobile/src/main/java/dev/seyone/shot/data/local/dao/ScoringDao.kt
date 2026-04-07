package dev.seyone.shot.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.seyone.shot.data.local.entity.ArrowEntity
import dev.seyone.shot.data.local.entity.EndEntity
import dev.seyone.shot.data.local.entity.EndWithArrows
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoringDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnd(end: EndEntity): Long

    @Update
    suspend fun updateEnd(end: EndEntity)

    @Delete
    suspend fun deleteEnd(end: EndEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArrows(arrows: List<ArrowEntity>)

    @Update
    suspend fun updateArrow(arrow: ArrowEntity)

    @Delete
    suspend fun deleteArrow(arrow: ArrowEntity)

    // @Transaction is needed when returning a @Relation POJO
    @Transaction
    @Query("SELECT * FROM ends WHERE sessionId = :sessionId ORDER BY sequenceOrder ASC")
    fun getEndsWithArrowsForSession(sessionId: Long): Flow<List<EndWithArrows>>

    @Transaction
    @Query("SELECT * FROM ends WHERE sessionId = :sessionId") // Adjust table name if necessary
    suspend fun getEndsWithArrowsForSessionSync(sessionId: Long): List<EndWithArrows>
}