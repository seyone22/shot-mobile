package dev.seyone.core.data.mapper

import dev.seyone.core.data.entity.SessionEntity
import dev.seyone.core.domain.model.Session

// --- SESSION MAPPER ---
fun SessionEntity.toDomainModel() = Session(
    id = id,
    roundId = roundId,
    bowId = bowId,
    arrowId = arrowId,
    locationId = locationId,
    sessionType = sessionType,
    inputMethod = inputMethod,
    numberOfArchers = numberOfArchers,
    arrowsPerEnd = arrowsPerEnd,
    notes = notes,
    timestamp = timestamp
    // Note: The 'round' and 'ends' properties in the Domain Model default to
    // null/emptyList(). They get populated later by your UI ViewModels
    // calling the RoundRepository and ScoringRepository.
)

fun Session.toEntity() = SessionEntity(
    id = id,
    roundId = roundId,
    bowId = bowId,
    arrowId = arrowId,
    locationId = locationId,
    sessionType = sessionType,
    inputMethod = inputMethod,
    numberOfArchers = numberOfArchers,
    arrowsPerEnd = arrowsPerEnd,
    notes = notes,
    timestamp = timestamp
)