package dev.seyone.core.domain.model

import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.ScoringMethod
import dev.seyone.core.domain.ShootingType
import dev.seyone.core.domain.TargetFaceSize

data class Distance(
    val id: Long = 0,
    val roundId: Long,
    val sequenceOrder: Int,
    val distanceValue: Int,
    val distanceUnit: DistanceUnit,
    val arrowsPerEnd: Int,
    val numberOfEnds: Int,
    val targetFaceSize: TargetFaceSize
)

// Replaces RoundEntity AND RoundWithDistances
data class Round(
    val id: Long = 0,
    val name: String,
    val category: String,
    val scoringMethod: ScoringMethod,
    val shootingType: ShootingType,
    val isCustom: Boolean = false,
    // Domain advantage: The round directly holds its distances
    val distances: List<Distance> = emptyList()
)