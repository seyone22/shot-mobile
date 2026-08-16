package dev.seyone.core.domain.repository

import dev.seyone.core.domain.model.SightMark
import kotlinx.coroutines.flow.Flow

interface SightMarkRepository {
    fun getSightMarksForBowStream(bowId: Long): Flow<List<SightMark>>
    fun getSightMarksForBowAndArrowStream(bowId: Long, arrowId: Long): Flow<List<SightMark>>
    suspend fun insertSightMark(sightMark: SightMark): Long
    suspend fun deleteSightMark(sightMark: SightMark)
    suspend fun getAllSightMarksSync(): List<SightMark>
}
