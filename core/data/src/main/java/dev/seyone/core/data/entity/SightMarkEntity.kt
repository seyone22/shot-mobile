package dev.seyone.core.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.model.SightMark

@Entity(
    tableName = "sight_marks",
    foreignKeys = [
        ForeignKey(
            entity = BowProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["bowProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["bowProfileId"]),
        Index(value = ["arrowSetId"])
    ]
)
data class SightMarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bowProfileId: Long,
    val arrowSetId: Long? = null,
    val drawWeightLbs: Float? = null,
    val distanceValue: Float,
    val distanceUnit: DistanceUnit = DistanceUnit.METERS,
    val elevationMark: Float,
    val windageMark: Float? = null,
    val notes: String = ""
)

fun SightMarkEntity.toDomainModel(): SightMark {
    return SightMark(
        id = id,
        bowProfileId = bowProfileId,
        arrowSetId = arrowSetId,
        drawWeightLbs = drawWeightLbs,
        distanceValue = distanceValue,
        distanceUnit = distanceUnit,
        elevationMark = elevationMark,
        windageMark = windageMark,
        notes = notes,
        isCalculated = false
    )
}

fun SightMark.toEntity(): SightMarkEntity {
    return SightMarkEntity(
        id = id,
        bowProfileId = bowProfileId,
        arrowSetId = arrowSetId,
        drawWeightLbs = drawWeightLbs,
        distanceValue = distanceValue,
        distanceUnit = distanceUnit,
        elevationMark = elevationMark,
        windageMark = windageMark,
        notes = notes
    )
}
