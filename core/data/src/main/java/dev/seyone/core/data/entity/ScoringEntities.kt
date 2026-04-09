package dev.seyone.core.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "ends",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // If a session is deleted, delete its ends
        )
    ],
    indices = [Index("sessionId")]
)
data class EndEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val sequenceOrder: Int, // e.g., End 1, End 2, End 3...
    val coachNotes: String? = null // For monitoring student progress and adding notes
)

@Entity(
    tableName = "arrows",
    foreignKeys = [
        ForeignKey(
            entity = EndEntity::class,
            parentColumns = ["id"],
            childColumns = ["endId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("endId")]
)
data class ArrowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val endId: Long,
    val sequenceOrder: Int,     // 1st arrow, 2nd arrow in the end
    val scoreValue: Int,        // Calculated score or manual input
    val isXRing: Boolean = false, // Special flag for inner-10/X ring
    val xCoordinate: Float? = null, // Visual target plotting X map
    val yCoordinate: Float? = null, // Visual target plotting Y map
    val physicalArrowId: String? = null // Unique ID for arrow fatigue tracking
)

// POJO to fetch an End and all its Arrows together
data class EndWithArrows(
    @Embedded val end: EndEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "endId"
    )
    val arrows: List<ArrowEntity>
)