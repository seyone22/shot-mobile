package dev.seyone.core.data.mapper

import dev.seyone.core.data.entity.DistanceEntity
import dev.seyone.core.data.entity.RoundEntity
import dev.seyone.core.data.entity.RoundWithDistances
import dev.seyone.core.domain.model.Distance
import dev.seyone.core.domain.model.Round

// --- DISTANCE MAPPER ---
fun DistanceEntity.toDomainModel() = Distance(
    id = id, roundId = roundId, sequenceOrder = sequenceOrder,
    distanceValue = distanceValue, distanceUnit = distanceUnit,
    arrowsPerEnd = arrowsPerEnd, numberOfEnds = numberOfEnds,
    targetFaceSize = targetFaceSize
)

fun Distance.toEntity() = DistanceEntity(
    id = id, roundId = roundId, sequenceOrder = sequenceOrder,
    distanceValue = distanceValue, distanceUnit = distanceUnit,
    arrowsPerEnd = arrowsPerEnd, numberOfEnds = numberOfEnds,
    targetFaceSize = targetFaceSize
)

// --- ROUND MAPPER ---
fun RoundEntity.toDomainModel(distances: List<Distance> = emptyList()) = Round(
    id = id, name = name, category = category, scoringMethod = scoringMethod,
    shootingType = shootingType, isCustom = isCustom,
    distances = distances // We attach the distances directly!
)

fun Round.toEntity() = RoundEntity(
    id = id, name = name, category = category, scoringMethod = scoringMethod,
    shootingType = shootingType, isCustom = isCustom
)

// --- MULTI-TABLE RELATION MAPPER ---
fun RoundWithDistances.toDomainModel(): Round {
    // 1. Map all the DistanceEntities to pure Kotlin Distances
    val mappedDistances = this.distances.map { it.toDomainModel() }

    // 2. Map the RoundEntity, and pass in the mapped distances
    return this.round.toDomainModel(distances = mappedDistances)
}