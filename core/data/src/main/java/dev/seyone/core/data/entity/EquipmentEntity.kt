package dev.seyone.core.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.seyone.core.domain.BowType
import dev.seyone.core.domain.ComponentCategory
import dev.seyone.core.domain.LocationType

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: LocationType = LocationType.OUTDOOR,
    val isDefault: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "bow_profiles")
data class BowProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g., "Primary Recurve"
    val bowType: BowType = BowType.RECURVE,
    val drawWeight: Float? = null, // in lbs
    val drawLength: Float? = null, // in inches
    val isDefault: Boolean = false,
    val notes: String = ""
)

// --- ARROW SET ---
@Entity(tableName = "arrow_sets")
data class ArrowSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val manufacturer: String = "",
    val model: String = "",
    val spine: Int? = null,
    val weight: Float? = null, // Stored as weightGrains in Domain
    val lengthInches: Float? = null, // New Field [cite: 18]
    val quantity: Int = 12,
    val shotCount: Int = 0,       // New Field for fatigue tracking [cite: 18, 28]
    val purchasePrice: Double? = null, // New Field
    val isDefault: Boolean = false,
    val notes: String = ""
)

@Entity(
    tableName = "bow_components",
    foreignKeys = [
        ForeignKey(
            entity = BowProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["bowProfileId"],
            onDelete = ForeignKey.CASCADE // If you delete the Bow Profile, delete its components
        )
    ],
    indices = [Index("bowProfileId")]
)
data class BowComponentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bowProfileId: Long, // Links this part to a specific bow
    val category: ComponentCategory,
    val brand: String = "",
    val model: String = "",
    val price: Double? = null,
    val notes: String = ""
)