package dev.seyone.core.domain.repository

import dev.seyone.core.domain.model.Arrow
import dev.seyone.core.domain.model.End
import kotlinx.coroutines.flow.Flow

interface ScoringRepository {
    // This now returns a list of fully constructed Ends, including their nested Arrows.
    fun getEndsForSessionStream(sessionId: Long): Flow<List<End>>

    // We just pass the End, which contains its Arrows.
    suspend fun insertEnd(end: End): Long

    suspend fun updateEnd(end: End): Int
    suspend fun updateArrow(arrow: Arrow): Int
    suspend fun insertArrows(arrows: List<Arrow>): List<Long>
    suspend fun deleteEnd(end: End): Int
    suspend fun deleteArrow(arrow: Arrow): Int

    // Synchronous fetch for analytics or exports
    suspend fun getAllArrowsForSessionSync(sessionId: Long): List<Arrow>
}