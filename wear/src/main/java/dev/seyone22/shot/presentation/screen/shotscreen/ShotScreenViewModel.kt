package dev.seyone22.shot.presentation.screen.shotscreen

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Shot(
    val score: Int = 0,              // e.g., 10, 9, 8 ... (or 0 for miss)
    val timestamp: Long = System.currentTimeMillis()
)

data class End(
    val shots: List<Shot> = emptyList()
) {
    val totalShots: Int
        get() = shots.size

    val endScore: Int
        get() = shots.sumOf { it.score }
}

data class ShotUiState(
    val ends: List<End> = emptyList()
) {
    val totalShots: Int
        get() = ends.sumOf { it.totalShots }

    val totalEnds: Int
        get() = ends.size
}

// Track actions for undo
sealed class ShotAction {
    data class AddShot(val endIndex: Int) : ShotAction()
    object NewEnd : ShotAction()
}

class ShotScreenViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ShotUiState())
    val uiState: StateFlow<ShotUiState> = _uiState.asStateFlow()

    private val actionStack = mutableListOf<ShotAction>()

    /** Increment arrow shot count in the current end */
    fun incrementArrow(score: Int = 0) {
        val currentState = _uiState.value
        val updatedEnds = if (currentState.ends.isEmpty()) {
            // first end, first shot
            listOf(End(shots = listOf(Shot(score))))
        } else {
            val lastEnd = currentState.ends.last()
            val updatedLastEnd = lastEnd.copy(
                shots = lastEnd.shots + Shot(score)
            )
            currentState.ends.dropLast(1) + updatedLastEnd
        }

        _uiState.value = currentState.copy(ends = updatedEnds)
        actionStack.add(ShotAction.AddShot(updatedEnds.lastIndex))
    }

    /** Start a new end */
    fun incrementEnd() {
        val currentState = _uiState.value
        val updatedEnds = currentState.ends + End()
        _uiState.value = currentState.copy(ends = updatedEnds)
        actionStack.add(ShotAction.NewEnd)
    }

    /** Undo last action (shot or end creation) */
    fun undoLastAction() {
        if (actionStack.isEmpty()) return

        val lastAction = actionStack.removeLast()
        val currentState = _uiState.value

        when (lastAction) {
            is ShotAction.AddShot -> {
                val endIndex = lastAction.endIndex
                val updatedEnd = currentState.ends[endIndex].copy(
                    shots = currentState.ends[endIndex].shots.dropLast(1)
                )
                val updatedEnds = currentState.ends.toMutableList().also {
                    it[endIndex] = updatedEnd
                }
                _uiState.value = currentState.copy(ends = updatedEnds)
            }
            ShotAction.NewEnd -> {
                _uiState.value = currentState.copy(ends = currentState.ends.dropLast(1))
            }
        }
    }

    /** Reset all counters */
    fun reset() {
        _uiState.value = ShotUiState()
        actionStack.clear()
    }
}
