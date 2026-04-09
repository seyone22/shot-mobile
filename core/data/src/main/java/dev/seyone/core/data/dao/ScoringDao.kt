package dev.seyone.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import dev.seyone.core.data.entity.ArrowEntity
import dev.seyone.core.data.entity.EndEntity
import dev.seyone.core.data.entity.EndWithArrows
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoringDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnd(end: EndEntity): Long

    @Update
    suspend fun updateEnd(end: EndEntity): Int

    @Delete
    suspend fun deleteEnd(end: EndEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArrows(arrows: List<ArrowEntity>): List<Long>

    @Update
    suspend fun updateArrow(arrow: ArrowEntity): Int

    @Delete
    suspend fun deleteArrow(arrow: ArrowEntity): Int

    // @Transaction is needed when returning a @Relation POJO
    @Transaction
    @Query("SELECT * FROM ends WHERE sessionId = :sessionId ORDER BY sequenceOrder ASC")
    fun getEndsWithArrowsForSession(sessionId: Long): Flow<List<EndWithArrows>>

    @Transaction
    @Query("SELECT * FROM ends WHERE sessionId = :sessionId") // Adjust table name if necessary
    suspend fun getEndsWithArrowsForSessionSync(sessionId: Long): List<EndWithArrows>
}