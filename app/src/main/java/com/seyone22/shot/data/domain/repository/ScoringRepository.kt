package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.entity.ArrowEntity
import com.seyone22.shot.data.local.entity.EndEntity
import com.seyone22.shot.data.local.entity.EndWithArrows
import kotlinx.coroutines.flow.Flow

interface ScoringRepository {
    fun getEndsWithArrowsForSession(sessionId: Long): Flow<List<EndWithArrows>>
    suspend fun insertEndWithArrows(end: EndEntity, arrows: List<ArrowEntity>): Long
    suspend fun updateEnd(end: EndEntity)
    suspend fun updateArrow(arrow: ArrowEntity)
    suspend fun insertArrows(arrows: List<ArrowEntity>)
    suspend fun deleteEnd(end: EndEntity)
    suspend fun deleteArrow(arrow: ArrowEntity)
}