package dev.seyone.core.domain.model

import dev.seyone.core.domain.AgeGroup
import dev.seyone.core.domain.BowType
import dev.seyone.core.domain.ComponentCategory
import dev.seyone.core.domain.Gender
import dev.seyone.core.domain.LocationType

data class Archer(
    val id: Long = 0,
    val name: String,
    val clubName: String? = null,
    val gender: Gender,
    val ageGroup: AgeGroup
)

data class Location(
    val id: Long = 0,
    val name: String,
    val type: LocationType = LocationType.OUTDOOR,
    val isDefault: Boolean = false,
    val notes: String = ""
)

data class BowComponent(
    val id: Long = 0,
    val bowProfileId: Long,
    val category: ComponentCategory,
    val brand: String = "",
    val model: String = "",
    val price: Double? = null,
    val notes: String = ""
)

data class BowProfile(
    val id: Long = 0,
    val name: String,
    val bowType: BowType = BowType.RECURVE,
    val drawWeight: Float? = null,
    val drawLength: Float? = null,
    val isDefault: Boolean = false,
    val notes: String = "",
    // Domain advantage: We can nest the components directly in the profile
    val components: List<BowComponent> = emptyList()
)

data class ArrowSet(
    val id: Long = 0,
    val name: String,             // e.g., "Indoor X10s"
    val manufacturer: String = "",// e.g., "Easton"
    val model: String = "",       // e.g., "X10 Protour"
    val spine: Int? = null,       // e.g., 380
    val weightGrains: Float? = null, // Total grain weight (arrow + point)
    val lengthInches: Float? = null, // e.g., 29.5
    val quantity: Int = 12,       // Current arrows in the set
    val shotCount: Int = 0,       // Automatically incremented during scoring
    val purchasePrice: Double? = null,
    val isDefault: Boolean = false,
    val notes: String = ""        // Fletching type, point weight, damage notes
)