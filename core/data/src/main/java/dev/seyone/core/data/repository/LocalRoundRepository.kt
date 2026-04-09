package dev.seyone.core.data.repository

import dev.seyone.core.data.dao.RoundDao
import dev.seyone.core.data.entity.DistanceEntity
import dev.seyone.core.data.entity.RoundEntity
import dev.seyone.core.data.entity.RoundWithDistances
import dev.seyone.core.domain.model.Distance
import dev.seyone.core.domain.model.Round
import dev.seyone.core.domain.repository.RoundRepository
import dev.seyone.core.data.mapper.* // Import your mappers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalRoundRepository(
    private val roundDao: RoundDao
) : RoundRepository {

    override fun getAllRoundsStream(): Flow<List<Round>> =
        roundDao.getAllRoundsWithDistances().map { list ->
            // Explicitly typed to avoid compiler confusion
            list.map { entity: RoundWithDistances -> entity.toDomainModel() }
        }

    override fun getRoundStream(id: Long): Flow<Round?> =
        roundDao.getRoundWithDistancesById(id).map { entity: RoundWithDistances? ->
            entity?.toDomainModel()
        }

    // Notice we only need to accept a Round now, not a Round AND a List!
    override suspend fun insertRound(round: Round): Long {
        // 1. Convert the core Round to an Entity
        val roundEntity = round.toEntity()

        // 2. Insert the Round and get its auto-generated ID from Room
        val generatedRoundId = roundDao.insertRound(roundEntity)

        // 3. Convert the Domain Distances to Entities, attaching the new roundId
        val distanceEntities = round.distances.map { distance ->
            distance.toEntity().copy(roundId = generatedRoundId)
        }

        // 4. Insert the Distances into the database
        if (distanceEntities.isNotEmpty()) {
            roundDao.insertDistances(distanceEntities)
        }

        return generatedRoundId
    }
}