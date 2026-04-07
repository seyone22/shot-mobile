package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.dao.ScoringDao
import com.seyone22.shot.data.local.entity.ArrowEntity
import com.seyone22.shot.data.local.entity.EndEntity
import com.seyone22.shot.data.local.entity.EndWithArrows
import kotlinx.coroutines.flow.Flow

class LocalScoringRepository(
    private val scoringDao: ScoringDao
) : ScoringRepository {

    override fun getEndsWithArrowsForSession(sessionId: Long): Flow<List<EndWithArrows>> {
        return scoringDao.getEndsWithArrowsForSession(sessionId)
    }

    override suspend fun insertEndWithArrows(end: EndEntity, arrows: List<ArrowEntity>): Long {
        val endId = scoringDao.insertEnd(end) // This returns the new ID
        val arrowsWithEndId = arrows.map { it.copy(endId = endId) }
        scoringDao.insertArrows(arrowsWithEndId)
        return endId // Return the ID back to the ViewModel
    }

    override suspend fun insertArrows(arrows: List<ArrowEntity>) {
        scoringDao.insertArrows(arrows)
    }

    override suspend fun updateEnd(end: EndEntity) = scoringDao.updateEnd(end)

    override suspend fun updateArrow(arrow: ArrowEntity) = scoringDao.updateArrow(arrow)

    override suspend fun deleteEnd(end: EndEntity) = scoringDao.deleteEnd(end)

    override suspend fun deleteArrow(arrow: ArrowEntity) = scoringDao.deleteArrow(arrow)

    override suspend fun getAllArrowsForSessionSync(sessionId: Long): List<ArrowEntity> {
        // Fetch the ends synchronously, then flatMap them to extract just the arrows
        return scoringDao.getEndsWithArrowsForSessionSync(sessionId).flatMap { it.arrows }
    }
}