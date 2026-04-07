package dev.seyone.shot.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// --- LOCATION ---
enum class LocationType { INDOOR, OUTDOOR, FIELD, ARCHERY_3D }

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: LocationType = LocationType.OUTDOOR,
    val isDefault: Boolean = false,
    val notes: String = ""
)

// --- BOW PROFILE ---
enum class BowType { RECURVE, COMPOUND, BAREBOW, LONGBOW, TRADITIONAL }

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
    val name: String, // e.g., "Easton X10 Outdoor Set"
    val manufacturer: String = "",
    val model: String = "",
    val spine: Int? = null, // e.g., 600
    val weight: Float? = null, // Total grain weight
    val quantity: Int = 12,
    val isDefault: Boolean = false,
    val notes: String = ""
)

enum class ComponentCategory {
    RISER, LIMBS, SIGHT, STRING, REST, PLUNGER, STABILIZER, CLICKER, OTHER
}

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