package dev.seyone.core.domain.model

import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.SessionType

data class Arrow(
    val id: Long = 0,
    val endId: Long,
    val sequenceOrder: Int,
    val scoreValue: Int,
    val isXRing: Boolean = false,
    val xCoordinate: Float? = null,
    val yCoordinate: Float? = null,
    val physicalArrowId: String? = null
)

// Replaces EndEntity AND EndWithArrows
data class End(
    val id: Long = 0,
    val sessionId: Long,
    val sequenceOrder: Int,
    val coachNotes: String? = null,
    // Domain advantage: The end directly holds its plotted arrows
    val arrows: List<Arrow> = emptyList()
)

data class Session(
    val id: Long = 0,
    val roundId: Long,
    val bowId: Long? = null,
    val arrowId: Long? = null,
    val locationId: Long? = null,
    val sessionType: SessionType,
    val inputMethod: InputMethod,
    val numberOfArchers: Int,
    val arrowsPerEnd: Int,
    val notes: String = "",
    val timestamp: Long,

    // Domain advantage: A fully constructed Session for the UI to observe
    val round: Round? = null,
    val ends: List<End> = emptyList()
)