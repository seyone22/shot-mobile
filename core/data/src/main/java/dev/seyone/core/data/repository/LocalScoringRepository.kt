package dev.seyone.core.data.repository

import dev.seyone.core.data.dao.ScoringDao
import dev.seyone.core.data.entity.EndWithArrows
import dev.seyone.core.data.mapper.toDomainModel
import dev.seyone.core.data.mapper.toEntity
import dev.seyone.core.domain.model.Arrow
import dev.seyone.core.domain.model.End
import dev.seyone.core.domain.repository.ScoringRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalScoringRepository(
    private val scoringDao: ScoringDao
) : ScoringRepository {

    override fun getEndsForSessionStream(sessionId: Long): Flow<List<End>> =
        scoringDao.getEndsWithArrowsForSession(sessionId).map { list ->
            list.map { entity: EndWithArrows -> entity.toDomainModel() }
        }

    // We only need to pass a single End. The arrows are already inside it!
    override suspend fun insertEnd(end: End): Long {
        // 1. Save the core End and get the DB-generated ID
        val endEntity = end.toEntity()
        val generatedEndId = scoringDao.insertEnd(endEntity)

        // 2. Attach that new ID to all the arrows before saving them
        val arrowEntities = end.arrows.map { arrow ->
            arrow.toEntity().copy(endId = generatedEndId)
        }

        // 3. Save the arrows
        if (arrowEntities.isNotEmpty()) {
            scoringDao.insertArrows(arrowEntities)
        }

        return generatedEndId
    }

    override suspend fun insertArrows(arrows: List<Arrow>): List<Long> {
        val entities = arrows.map { it.toEntity() }
        return scoringDao.insertArrows(entities)
    }

    override suspend fun updateEnd(end: End) = scoringDao.updateEnd(end.toEntity())

    override suspend fun updateArrow(arrow: Arrow) = scoringDao.updateArrow(arrow.toEntity())

    override suspend fun deleteEnd(end: End) = scoringDao.deleteEnd(end.toEntity())

    override suspend fun deleteArrow(arrow: Arrow) = scoringDao.deleteArrow(arrow.toEntity())

    override suspend fun getAllArrowsForSessionSync(sessionId: Long): List<Arrow> {
        // Fetch the EndWithArrows synchronously, map the Entities to Domain models,
        // and flatten them into a single continuous list of Arrows.
        return scoringDao.getEndsWithArrowsForSessionSync(sessionId).flatMap { endWithArrows ->
            endWithArrows.arrows.map { arrowEntity -> arrowEntity.toDomainModel() }
        }
    }
}