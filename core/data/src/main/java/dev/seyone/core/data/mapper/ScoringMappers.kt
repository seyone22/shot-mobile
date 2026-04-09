package dev.seyone.core.data.mapper

import dev.seyone.core.data.entity.ArrowEntity
import dev.seyone.core.data.entity.EndEntity
import dev.seyone.core.data.entity.EndWithArrows
import dev.seyone.core.domain.model.Arrow
import dev.seyone.core.domain.model.End

// --- ARROW MAPPER ---
fun ArrowEntity.toDomainModel() = Arrow(
    id = id, endId = endId, sequenceOrder = sequenceOrder,
    scoreValue = scoreValue, isXRing = isXRing,
    xCoordinate = xCoordinate, yCoordinate = yCoordinate,
    physicalArrowId = physicalArrowId
)

fun Arrow.toEntity() = ArrowEntity(
    id = id, endId = endId, sequenceOrder = sequenceOrder,
    scoreValue = scoreValue, isXRing = isXRing,
    xCoordinate = xCoordinate, yCoordinate = yCoordinate,
    physicalArrowId = physicalArrowId
)

// --- END MAPPER ---
fun EndEntity.toDomainModel(arrows: List<Arrow> = emptyList()) = End(
    id = id, sessionId = sessionId, sequenceOrder = sequenceOrder,
    coachNotes = coachNotes,
    arrows = arrows // Attach the nested list!
)

fun End.toEntity() = EndEntity(
    id = id, sessionId = sessionId, sequenceOrder = sequenceOrder,
    coachNotes = coachNotes
)

// --- MULTI-TABLE RELATION MAPPER ---
fun EndWithArrows.toDomainModel(): End {
    // 1. Map all the ArrowEntities to pure Kotlin Arrows
    val mappedArrows = this.arrows.map { it.toDomainModel() }

    // 2. Map the EndEntity, and attach the mapped arrows to it
    return this.end.toDomainModel(arrows = mappedArrows)
}