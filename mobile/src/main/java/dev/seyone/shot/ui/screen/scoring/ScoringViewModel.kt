package dev.seyone.shot.ui.screen.scoring

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.shot.data.domain.repository.RoundRepository
import dev.seyone.shot.data.domain.repository.ScoringRepository
import dev.seyone.shot.data.domain.repository.SessionRepository
import dev.seyone.shot.data.local.entity.ArrowEntity
import dev.seyone.shot.data.local.entity.EndEntity
import dev.seyone.shot.data.local.entity.InputMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class ArrowInput(val value: String, val score: Int, val isX: Boolean = false, val dbId: Long = 0L)

data class ScoringUiState(
    val title: String = "Loading...",
    val distance: String = "...",
    val golds: Int = 0,
    val average: Float = 0.0f,
    val totalScore: Int = 0,
    val ends: List<List<ArrowInput>> = listOf(emptyList()),
    val currentEndIndex: Int = 0,
    val inputMethod: InputMethod = InputMethod.ARROW_VALUES,
    val arrowsPerEnd: Int = 6, // Safe default until DB loads
    val activeEndDbId: Long? = null,
    val currentEndHits: List<Offset> = emptyList()
)

class ScoringViewModel(
    private val sessionId: Long,
    private val scoringRepository: ScoringRepository,
    private val sessionRepository: SessionRepository,
    private val roundRepository: RoundRepository // <-- ADDED to fetch round/distance configs
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
        observeDatabase()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            val session = sessionRepository.getSessionStream(sessionId).firstOrNull()

            if (session != null) {
                val roundWithDistances = roundRepository.getRoundWithDistancesStream(session.roundId).firstOrNull()

                if (roundWithDistances != null) {
                    val firstDistance = roundWithDistances.distances.firstOrNull()
                    val distanceText = firstDistance?.let {
                        "${it.distanceValue}${it.distanceUnit.name.take(1).lowercase()}"
                    } ?: "Target"

                    _uiState.update { state ->
                        state.copy(
                            title = roundWithDistances.round.name,
                            distance = distanceText,
                            inputMethod = session.inputMethod,
                            arrowsPerEnd = session.arrowsPerEnd // <--- Read it directly from the Session!
                        )
                    }
                }
            }

            // 3. Initialize or Resume the Database Ends
            val existingEnds = scoringRepository.getEndsWithArrowsForSession(sessionId).firstOrNull()

            if (existingEnds.isNullOrEmpty()) {
                // New Session: Create first end
                val firstEndId = scoringRepository.insertEndWithArrows(
                    end = EndEntity(sessionId = sessionId, sequenceOrder = 1),
                    arrows = emptyList()
                )
                _uiState.update { it.copy(activeEndDbId = firstEndId) }
            } else {
                // Resumed Session: Target the last existing end
                val lastEnd = existingEnds.last().end
                _uiState.update { it.copy(activeEndDbId = lastEnd.id) }
            }
        }
    }

    fun onTargetInput(offset: Offset, targetRadius: Float) {
        // 1. Calculate the distance from center (0 to 1 range)
        val dx = offset.x - targetRadius
        val dy = offset.y - targetRadius
        val distance = sqrt(dx * dx + dy * dy)
        val normalizedDistance = (distance / targetRadius).coerceIn(0f, 1f)

        // 2. Map distance to Score (World Archery 10-ring scoring)
        // 0.0 - 0.1: 10/X, 0.1 - 0.2: 9, etc.
        val score = when {
            normalizedDistance <= 0.05f -> 10 // X-ring
            normalizedDistance <= 0.1f -> 10
            normalizedDistance <= 0.2f -> 9
            normalizedDistance <= 0.3f -> 8
            normalizedDistance <= 0.4f -> 7
            normalizedDistance <= 0.5f -> 6
            normalizedDistance <= 0.6f -> 5
            normalizedDistance <= 0.7f -> 4
            normalizedDistance <= 0.8f -> 3
            normalizedDistance <= 0.9f -> 2
            normalizedDistance <= 1.0f -> 1
            else -> 0
        }

        val displayValue = if (normalizedDistance <= 0.05f) "X" else if (score == 0) "M" else score.toString()
        val isX = normalizedDistance <= 0.05f

        // 3. Reuse your existing arrow input logic
        onArrowInput(displayValue, score, isX)

        _uiState.update { state ->
            // Append the new coordinate to the visual hit list
            state.copy(
                currentEndHits = state.currentEndHits + offset
            )
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            scoringRepository.getEndsWithArrowsForSession(sessionId).collect { dbEnds ->
                if (dbEnds.isEmpty()) return@collect

                val mappedEnds = dbEnds.map { endWithArrows ->
                    endWithArrows.arrows.map { arrow ->
                        val displayValue = when {
                            arrow.isXRing -> "X"
                            arrow.scoreValue == 0 -> "M"
                            else -> arrow.scoreValue.toString()
                        }
                        ArrowInput(displayValue, arrow.scoreValue, arrow.isXRing, arrow.id)
                    }
                }

                val allArrows = mappedEnds.flatten()
                val totalScore = allArrows.sumOf { it.score }
                val golds = allArrows.count { it.value == "10" || it.value == "X" }
                val avg = if (allArrows.isNotEmpty()) totalScore.toFloat() / allArrows.size else 0f

                _uiState.update { state ->
                    state.copy(
                        ends = mappedEnds,
                        totalScore = totalScore,
                        golds = golds,
                        average = avg,
                        currentEndIndex = mappedEnds.size - 1,
                        activeEndDbId = dbEnds.last().end.id
                    )
                }
            }
        }
    }

    fun onArrowInput(value: String, score: Int, isX: Boolean = false) {
        viewModelScope.launch {
            val state = _uiState.value
            val currentArrows = state.ends.getOrNull(state.currentEndIndex) ?: emptyList()

            if (currentArrows.size < state.arrowsPerEnd && state.activeEndDbId != null) {
                val newArrow = ArrowEntity(
                    endId = state.activeEndDbId,
                    sequenceOrder = currentArrows.size + 1,
                    scoreValue = score,
                    isXRing = isX
                )

                scoringRepository.insertArrows(listOf(newArrow))

                // Auto-Advance Logic using the REAL arrowsPerEnd
                if (currentArrows.size + 1 == state.arrowsPerEnd) {
                    scoringRepository.insertEndWithArrows(
                        end = EndEntity(sessionId = sessionId, sequenceOrder = state.currentEndIndex + 2),
                        arrows = emptyList()
                    )
                }
            }
        }
    }

    fun onBackspace() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentArrows = state.ends.getOrNull(state.currentEndIndex) ?: emptyList()

            if (currentArrows.isNotEmpty()) {
                val arrowToDeleteId = currentArrows.last().dbId
                scoringRepository.deleteArrow(ArrowEntity(id = arrowToDeleteId, endId = state.activeEndDbId!!, sequenceOrder = currentArrows.size, scoreValue = 0))
            } else if (state.currentEndIndex > 0) {
                scoringRepository.deleteEnd(EndEntity(id = state.activeEndDbId!!, sessionId = sessionId, sequenceOrder = state.currentEndIndex + 1))
            }
        }
    }

    fun onNextEnd() {
        viewModelScope.launch {
            val state = _uiState.value
            scoringRepository.insertEndWithArrows(
                end = EndEntity(sessionId = sessionId, sequenceOrder = state.currentEndIndex + 2),
                arrows = emptyList()
            )
            _uiState.update { it.copy(currentEndHits = emptyList()) }
        }
    }

    class Factory(
        private val sessionId: Long,
        private val scoringRepository: ScoringRepository,
        private val sessionRepository: SessionRepository,
        private val roundRepository: RoundRepository // <-- ADDED
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScoringViewModel(sessionId, scoringRepository, sessionRepository, roundRepository) as T
        }
    }
}