package dev.seyone.shot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "distances",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roundId")] // Indexing foreign keys is a strict Google recommendation for performance
)
data class DistanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long, // Links back to RoundEntity
    val sequenceOrder: Int, // e.g., 1 for 90m, 2 for 70m, etc.
    val distanceValue: Int,
    val distanceUnit: DistanceUnit,
    val arrowsPerEnd: Int,
    val numberOfEnds: Int,
    val targetFaceSize: TargetFaceSize
)