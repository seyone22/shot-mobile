package dev.seyone.core.data.repository

import dev.seyone.core.data.dao.SessionDao
import dev.seyone.core.data.entity.SessionEntity
import dev.seyone.core.domain.model.Session
import dev.seyone.core.domain.repository.SessionRepository
import dev.seyone.core.data.mapper.* // Import your mappers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSessionRepository(
    private val sessionDao: SessionDao
) : SessionRepository {

    override fun getAllSessionsStream(): Flow<List<Session>> =
        sessionDao.getAllSessionsStream().map { list ->
            list.map { entity: SessionEntity -> entity.toDomainModel() }
        }

    override fun getSessionStream(id: Long): Flow<Session?> =
        sessionDao.getSessionStream(id).map { entity: SessionEntity? ->
            entity?.toDomainModel()
        }

    override suspend fun insertSession(session: Session): Long =
        sessionDao.insertSession(session.toEntity())

    override suspend fun updateSession(session: Session): Int =
        sessionDao.updateSession(session.toEntity())

    override suspend fun deleteSession(session: Session): Int =
        sessionDao.deleteSession(session.toEntity())
}