package dev.seyone.shot.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.seyone.shot.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    // <-- ADD THIS FUNCTION -->
    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSession(id: Long): Flow<SessionEntity?>

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAllSessionsStream(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getSessionStream(id: Long): Flow<SessionEntity?>
}