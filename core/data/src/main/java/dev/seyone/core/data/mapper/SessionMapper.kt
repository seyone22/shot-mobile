package dev.seyone.core.data.mapper

import dev.seyone.core.data.entity.SessionEntity
import dev.seyone.core.domain.model.Session

// --- SESSION MAPPER ---
fun SessionEntity.toDomainModel() = Session(
    id = id,
    roundId = roundId,
    archerId = archerId,
    bowId = bowId,
    arrowId = arrowId,
    locationId = locationId,
    sessionType = sessionType,
    inputMethod = inputMethod,
    numberOfArchers = numberOfArchers,
    arrowsPerEnd = arrowsPerEnd,
    name = name,
    notes = notes,
    timestamp = timestamp
)

fun Session.toEntity() = SessionEntity(
    id = id,
    roundId = roundId,
    archerId = archerId,
    bowId = bowId,
    arrowId = arrowId,
    locationId = locationId,
    sessionType = sessionType,
    inputMethod = inputMethod,
    numberOfArchers = numberOfArchers,
    arrowsPerEnd = arrowsPerEnd,
    name = name,
    notes = notes,
    timestamp = timestamp
)