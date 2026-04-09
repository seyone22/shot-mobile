package dev.seyone.core.data.mapper

import dev.seyone.core.data.entity.ArrowSetEntity
import dev.seyone.core.data.entity.BowComponentEntity
import dev.seyone.core.data.entity.BowProfileEntity
import dev.seyone.core.data.entity.LocationEntity
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.model.BowComponent
import dev.seyone.core.domain.model.BowProfile
import dev.seyone.core.domain.model.Location

// --- LOCATION MAPPER ---
fun LocationEntity.toDomainModel() = Location(
    id = id, name = name, type = type, isDefault = isDefault, notes = notes
)
fun Location.toEntity() = LocationEntity(
    id = id, name = name, type = type, isDefault = isDefault, notes = notes
)

// --- ARROW SET MAPPER ---
fun ArrowSetEntity.toDomainModel() = ArrowSet(
    id = id,
    name = name,
    manufacturer = manufacturer,
    model = model,
    spine = spine,
    weightGrains = weight,
    lengthInches = lengthInches, // Map New Field
    quantity = quantity,
    shotCount = shotCount,       // Map New Field
    purchasePrice = purchasePrice, // Map New Field
    isDefault = isDefault,
    notes = notes
)

fun ArrowSet.toEntity() = ArrowSetEntity(
    id = id,
    name = name,
    manufacturer = manufacturer,
    model = model,
    spine = spine,
    weight = weightGrains,
    lengthInches = lengthInches, // Map New Field
    quantity = quantity,
    shotCount = shotCount,       // Map New Field
    purchasePrice = purchasePrice, // Map New Field
    isDefault = isDefault,
    notes = notes
)

// --- BOW COMPONENT MAPPER ---
fun BowComponentEntity.toDomainModel() = BowComponent(
    id = id, bowProfileId = bowProfileId, category = category,
    brand = brand, model = model, price = price, notes = notes
)
fun BowComponent.toEntity() = BowComponentEntity(
    id = id, bowProfileId = bowProfileId, category = category,
    brand = brand, model = model, price = price, notes = notes
)

// --- BOW PROFILE MAPPER ---
fun BowProfileEntity.toDomainModel() = BowProfile(
    id = id, name = name, bowType = bowType, drawWeight = drawWeight,
    drawLength = drawLength, isDefault = isDefault, notes = notes
    // Note: 'components' defaults to emptyList() here. If you need them
    // bundled together later, you will create a BowProfileWithComponents POJO.
)
fun BowProfile.toEntity() = BowProfileEntity(
    id = id, name = name, bowType = bowType, drawWeight = drawWeight,
    drawLength = drawLength, isDefault = isDefault, notes = notes
)