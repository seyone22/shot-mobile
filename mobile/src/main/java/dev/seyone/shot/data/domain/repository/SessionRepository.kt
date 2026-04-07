package dev.seyone.shot.data.domain.repository

import dev.seyone.shot.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessionsStream(): Flow<List<SessionEntity>>
    fun getSessionStream(id: Long): Flow<SessionEntity?>
    suspend fun insertSession(session: SessionEntity): Long
    suspend fun updateSession(session: SessionEntity)
    suspend fun deleteSession(session: SessionEntity)
}