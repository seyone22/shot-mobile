package dev.seyone.shot.data.domain.repository

import dev.seyone.shot.data.local.entity.DistanceEntity
import dev.seyone.shot.data.local.entity.RoundEntity
import dev.seyone.shot.data.local.entity.RoundWithDistances
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