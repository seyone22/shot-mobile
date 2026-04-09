package dev.seyone.core.domain.repository

import dev.seyone.core.domain.model.Round
import kotlinx.coroutines.flow.Flow

interface RoundRepository {
    fun getAllRoundsStream(): Flow<List<Round>>

    // This now returns a fully constructed Round, including its Distances.
    fun getRoundStream(id: Long): Flow<Round?>

    // Because Round contains List<Distance>, we only need to pass the Round.
    // The implementation in :core:data will handle splitting it into Entities.
    suspend fun insertRound(round: Round): Long
}