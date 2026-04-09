package dev.seyone.core.domain.repository

import dev.seyone.core.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessionsStream(): Flow<List<Session>>
    fun getSessionStream(id: Long): Flow<Session?>
    suspend fun insertSession(session: Session): Long
    suspend fun updateSession(session: Session): Int
    suspend fun deleteSession(session: Session): Int
}