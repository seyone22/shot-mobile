package dev.seyone.shot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rounds")
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,          // e.g., "WA 1440 (90m)" or "WA 18m"
    val category: String,      // e.g., "WA (International)", "GNAS (Metric)", "Custom"
    val scoringMethod: ScoringMethod,
    val shootingType: ShootingType,
    val isCustom: Boolean = false
)