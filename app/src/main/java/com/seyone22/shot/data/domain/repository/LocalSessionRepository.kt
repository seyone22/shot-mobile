package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.dao.SessionDao
import com.seyone22.shot.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class LocalSessionRepository(
    private val sessionDao: SessionDao
) : SessionRepository {
    override fun getAllSessionsStream(): Flow<List<SessionEntity>> = sessionDao.getAllSessionsStream()
    override fun getSessionStream(id: Long): Flow<SessionEntity?> = sessionDao.getSessionStream(id)
    override suspend fun insertSession(session: SessionEntity): Long = sessionDao.insertSession(session)
    override suspend fun updateSession(session: SessionEntity) = sessionDao.updateSession(session)
    override suspend fun deleteSession(session: SessionEntity) = sessionDao.deleteSession(session)
}