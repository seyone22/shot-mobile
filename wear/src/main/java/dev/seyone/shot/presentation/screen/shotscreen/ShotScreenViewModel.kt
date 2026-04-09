package dev.seyone.shot.presentation.screen.shotscreen

import androidx.lifecycle.ViewModel
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.model.Arrow
import dev.seyone.core.domain.model.End
import dev.seyone.core.domain.model.Session
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WearSessionUiState(
    val activeSession: Session? = null,
    val currentEndArrows: List<Arrow> = emptyList(),
    val liveHeartRate: Int = 85 // Mocked biometric data
) {
    // Computed domain properties for UI
    val totalScore: Int
        get() = (activeSession?.ends?.sumOf { end -> end.arrows.sumOf { it.scoreValue } } ?: 0) +
                currentEndArrows.sumOf { it.scoreValue }

    val totalArrowCount: Int
        get() = (activeSession?.ends?.sumOf { it.arrows.size } ?: 0) + currentEndArrows.size

    val averageArrow: Float
        get() = if (totalArrowCount == 0) 0f else totalScore.toFloat() / totalArrowCount
}

class ShotScreenViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WearSessionUiState())
    val uiState: StateFlow<WearSessionUiState> = _uiState.asStateFlow()

    fun loadSession(session: Session) {
        _uiState.update {
            it.copy(activeSession = session, currentEndArrows = emptyList())
        }
    }

    fun addArrow(score: Int, isX: Boolean = false, xCoord: Float? = null, yCoord: Float? = null) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state

            // Construct the domain Arrow
            val newArrow = Arrow(
                endId = 0, // In-memory placeholder
                sequenceOrder = state.currentEndArrows.size + 1,
                scoreValue = score,
                isXRing = isX,
                xCoordinate = xCoord,
                yCoordinate = yCoord
            )
            state.copy(currentEndArrows = state.currentEndArrows + newArrow)
        }
    }

    fun completeEnd() {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            if (state.currentEndArrows.isEmpty()) return@update state

            // Construct the domain End
            val newEnd = End(
                sessionId = session.id,
                sequenceOrder = session.ends.size + 1,
                arrows = state.currentEndArrows
            )

            // Append End to Session
            val updatedSession = session.copy(ends = session.ends + newEnd)

            state.copy(
                activeSession = updatedSession,
                currentEndArrows = emptyList() // Reset for the next end
            )
        }
    }

    fun undoLastAction() {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state

            if (state.currentEndArrows.isNotEmpty()) {
                // Undo the last arrow shot
                state.copy(currentEndArrows = state.currentEndArrows.dropLast(1))
            } else if (session.ends.isNotEmpty()) {
                // Re-open the previous end
                val lastEnd = session.ends.last()
                val updatedSession = session.copy(ends = session.ends.dropLast(1))

                state.copy(
                    activeSession = updatedSession,
                    currentEndArrows = lastEnd.arrows.dropLast(1) // Drop the last arrow of that re-opened end
                )
            } else {
                state
            }
        }
    }
}