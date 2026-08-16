package dev.seyone.core.data.repository

import dev.seyone.core.data.dao.SightMarkDao
import dev.seyone.core.data.entity.toDomainModel
import dev.seyone.core.data.entity.toEntity
import dev.seyone.core.domain.model.SightMark
import dev.seyone.core.domain.repository.SightMarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalSightMarkRepository(
    private val sightMarkDao: SightMarkDao
) : SightMarkRepository {

    override fun getSightMarksForBowStream(bowId: Long): Flow<List<SightMark>> {
        return sightMarkDao.getSightMarksForBowStream(bowId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getSightMarksForBowAndArrowStream(bowId: Long, arrowId: Long): Flow<List<SightMark>> {
        return sightMarkDao.getSightMarksForBowAndArrowStream(bowId, arrowId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun insertSightMark(sightMark: SightMark): Long {
        return sightMarkDao.insertSightMark(sightMark.toEntity())
    }

    override suspend fun deleteSightMark(sightMark: SightMark) {
        sightMarkDao.deleteSightMark(sightMark.toEntity())
    }

    override suspend fun getAllSightMarksSync(): List<SightMark> {
        return sightMarkDao.getAllSightMarksSync().map { it.toDomainModel() }
    }
}
