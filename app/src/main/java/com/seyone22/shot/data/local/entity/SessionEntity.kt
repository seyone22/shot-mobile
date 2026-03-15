package com.seyone22.shot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.RESTRICT // Don't let users delete a round if a session uses it
        )
        // Note: Foreign keys for Bow, Arrow, and Location will go here once we build those entities.
    ],
    indices = [Index("roundId")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val bowId: Long?,      // Nullable for now
    val arrowId: Long?,    // Nullable for now
    val locationId: Long?, // Nullable for now
    val sessionType: SessionType,
    val inputMethod: InputMethod,
    val numberOfArchers: Int,
    val arrowsPerEnd: Int, // <--- ADD THIS LINE
    val notes: String = "", // <--- ADD THIS
    val timestamp: Long = System.currentTimeMillis() // Standard way to store dates in Room
)