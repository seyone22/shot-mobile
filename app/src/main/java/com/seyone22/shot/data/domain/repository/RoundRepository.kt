package com.seyone22.shot.data.domain.repository

import com.seyone22.shot.data.local.entity.DistanceEntity
import com.seyone22.shot.data.local.entity.RoundEntity
import com.seyone22.shot.data.local.entity.RoundWithDistances
import kotlinx.coroutines.flow.Flow

/**
 * Interface detailing all round and distance related operations.
 */
interface RoundRepository {
    fun getAllRoundsStream(): Flow<List<RoundWithDistances>>
    fun getRoundStream(id: Long): Flow<RoundWithDistances?>
    suspend fun insertRoundWithDistances(round: RoundEntity, distances: List<DistanceEntity>)

    // <-- ADD THIS -->
    fun getRoundWithDistancesStream(id: Long): Flow<RoundWithDistances?>
}