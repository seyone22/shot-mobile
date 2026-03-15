package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getAllSessionsStream(): Flow<List<SessionEntity>>
    fun getSessionStream(id: Long): Flow<SessionEntity?>
    suspend fun insertSession(session: SessionEntity): Long
    suspend fun updateSession(session: SessionEntity)
    suspend fun deleteSession(session: SessionEntity)
}