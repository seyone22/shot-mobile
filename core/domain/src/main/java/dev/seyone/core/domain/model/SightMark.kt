package dev.seyone.core.domain.model

import dev.seyone.core.domain.DistanceUnit

data class SightMark(
    val id: Long = 0,
    val bowProfileId: Long,
    val arrowSetId: Long? = null,
    val drawWeightLbs: Float? = null,
    val distanceValue: Float,
    val distanceUnit: DistanceUnit = DistanceUnit.METERS,
    val elevationMark: Float,
    val windageMark: Float? = null,
    val notes: String = "",
    val isCalculated: Boolean = false
)
