package dev.seyone.shot.ui.screen.scoring

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.TargetFaceSize
import dev.seyone.core.domain.model.Arrow
import dev.seyone.core.domain.model.Distance
import dev.seyone.core.domain.model.End
import dev.seyone.core.domain.repository.RoundRepository
import dev.seyone.core.domain.repository.ScoringRepository
import dev.seyone.core.domain.repository.SessionRepository
import dev.seyone.shot.ui.screen.scoring.components.calculateArrowScore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArrowInput(
    val value: String,
    val score: Int,
    val isX: Boolean = false,
    val dbId: Long = 0L,
    val xCoord: Float? = null,
    val yCoord: Float? = null
)

data class ScoringUiState(
    val title: String = "Loading...",
    val golds: Int = 0,
    val average: Float = 0.0f,
    val totalScore: Int = 0,

    val ends: List<List<ArrowInput>> = listOf(emptyList()),
    val endDbIds: List<Long> = emptyList(),
    val currentEndIndex: Int = 0,

    val selectedEndIndex: Int? = null,
    val selectedArrowIndex: Int? = null,

    val inputMethod: InputMethod = InputMethod.ARROW_VALUES,
    val activeEndDbId: Long? = null,
    val currentEndHits: List<Offset> = emptyList(),

    // --- Multi-Distance Support ---
    val distances: List<Distance> = emptyList(),
    val activeDistanceText: String = "...",
    val activeArrowsPerEnd: Int = 6,
    val activeScoringMethod: String = "METRIC_10_ZONE",
    val activeTargetFace: TargetFaceSize = TargetFaceSize.CM_122
)

sealed interface ScoringUiEvent {
    data class ShowToast(val message: String) : ScoringUiEvent // <-- No () here
}

class ScoringViewModel(
    private val sessionId: Long,
    private val scoringRepository: ScoringRepository,
    private val sessionRepository: SessionRepository,
    private val roundRepository: RoundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoringUiState())
    val uiState: StateFlow<ScoringUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<ScoringUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadSessionData()
        observeDatabase()
    }

    private fun canAddNewEnd(state: ScoringUiState): Boolean {
        // Custom rounds with no defined distances have no cap
        if (state.distances.isEmpty()) return true

        val totalAllowedEnds = state.distances.sumOf { it.numberOfEnds }
        // e.g., If current index is 19 (the 20th end), 19 + 1 < 20 is FALSE.
        return (state.currentEndIndex + 1) < totalAllowedEnds
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            val session = sessionRepository.getSessionStream(sessionId).firstOrNull()

            if (session != null) {
                val roundWithDistances =
                    roundRepository.getRoundStream(session.roundId).firstOrNull()

                if (roundWithDistances != null) {
                    // Sort distances to ensure sequence accuracy
                    val sortedDistances = roundWithDistances.distances.sortedBy { it.sequenceOrder }

                    _uiState.update { state ->
                        state.copy(
                            title = roundWithDistances.name,
                            inputMethod = session.inputMethod,
                            distances = sortedDistances
                        )
                    }
                }
            }

            val existingEnds = scoringRepository.getEndsForSessionStream(sessionId).firstOrNull()

            if (existingEnds.isNullOrEmpty()) {
                val firstEndId = scoringRepository.insertEnd(
                    end = End(sessionId = sessionId, sequenceOrder = 1)
                )
                _uiState.update { it.copy(activeEndDbId = firstEndId) }
            } else {
                val lastEnd = existingEnds.last()
                _uiState.update { it.copy(activeEndDbId = lastEnd.id) }
            }
        }
    }

    private fun getActiveDistanceConfig(distances: List<Distance>, endIndex: Int): Distance? {
        if (distances.isEmpty()) return null

        var endsCounted = 0
        for (distance in distances) {
            endsCounted += distance.numberOfEnds
            if (endIndex < endsCounted) {
                return distance
            }
        }
        return distances.last()
    }

    fun onTargetInput(
        offset: Offset,
        targetRadius: Float,
        targetConfig: TargetFaceSize // Usually passed from UI based on state.activeTargetFace
    ) {
        val currentState = _uiState.value
        val center = Offset(targetRadius, targetRadius)

        val displayValue = calculateArrowScore(
            tapOffset = offset,
            center = center,
            targetRadius = targetRadius,
            targetFace = targetConfig,
            scoringMethod = currentState.activeScoringMethod // <-- Uses dynamic distance math
        )

        val numericScore = when (displayValue) {
            "X" -> 10
            "M" -> 0
            else -> displayValue.toIntOrNull() ?: 0
        }
        val isX = displayValue == "X"

        onArrowInput(displayValue, numericScore, isX, offset.x, offset.y)
    }

    fun selectArrowForEdit(endIndex: Int, arrowIndex: Int) {
        _uiState.update { state ->
            val deselecting =
                state.selectedEndIndex == endIndex && state.selectedArrowIndex == arrowIndex
            val newSelectedEnd = if (deselecting) null else endIndex
            val newSelectedArrow = if (deselecting) null else arrowIndex

            // Swap out the target face dots for the row being viewed!
            val targetEndIndex = newSelectedEnd ?: state.currentEndIndex
            val hits = state.ends.getOrNull(targetEndIndex)?.mapNotNull {
                if (it.xCoord != null && it.yCoord != null) Offset(it.xCoord, it.yCoord) else null
            } ?: emptyList()

            state.copy(
                selectedEndIndex = newSelectedEnd,
                selectedArrowIndex = newSelectedArrow,
                currentEndHits = hits
            )
        }
    }

    fun onArrowInput(
        value: String,
        score: Int,
        isX: Boolean = false,
        xCoord: Float? = null,
        yCoord: Float? = null
    ) {
        viewModelScope.launch {
            val state = _uiState.value

            // CASE A: EDITING an existing arrow or INSERTING into an empty slot
            if (state.selectedEndIndex != null && state.selectedArrowIndex != null) {
                val endToEdit = state.ends.getOrNull(state.selectedEndIndex) ?: return@launch
                val arrowToEdit = endToEdit.getOrNull(state.selectedArrowIndex)
                val historicalEndDbId =
                    state.endDbIds.getOrNull(state.selectedEndIndex) ?: return@launch

                if (arrowToEdit != null) {
                    // Update existing
                    val updatedArrow = Arrow(
                        id = arrowToEdit.dbId,
                        endId = historicalEndDbId,
                        sequenceOrder = state.selectedArrowIndex + 1,
                        scoreValue = score,
                        isXRing = isX,
                        xCoordinate = xCoord ?: arrowToEdit.xCoord,
                        yCoordinate = yCoord ?: arrowToEdit.yCoord
                    )
                    scoringRepository.updateArrow(updatedArrow)
                } else {
                    // Insert into empty slot
                    val newArrow = Arrow(
                        endId = historicalEndDbId,
                        sequenceOrder = state.selectedArrowIndex + 1,
                        scoreValue = score,
                        isXRing = isX,
                        xCoordinate = xCoord,
                        yCoordinate = yCoord
                    )
                    scoringRepository.insertArrows(listOf(newArrow))
                }

                // Auto-advance
                val nextArrowIndex = state.selectedArrowIndex + 1
                if (nextArrowIndex < state.activeArrowsPerEnd) {
                    _uiState.update { it.copy(selectedArrowIndex = nextArrowIndex) }
                } else {
                    _uiState.update { it.copy(selectedEndIndex = null, selectedArrowIndex = null) }

                    if (state.selectedEndIndex == state.currentEndIndex && canAddNewEnd(state)) {
                        scoringRepository.insertEnd(
                            end = End(sessionId = sessionId, sequenceOrder = state.currentEndIndex + 2)
                        )
                    }

                    // Auto-generate next end if we filled the last slot of the current end
                    if (state.selectedEndIndex == state.currentEndIndex) {
                        if (canAddNewEnd(state)) {
                            scoringRepository.insertEnd(
                                end = End(sessionId = sessionId, sequenceOrder = state.currentEndIndex + 2)
                            )
                        } else {
                            // --- NEW: Trigger Toast on Edit Complete! ---
                            _uiEvent.send(ScoringUiEvent.ShowToast("Round Complete! 🎯"))
                        }
                    }
                }
                return@launch
            }

            // CASE B: Standard APPEND behavior
            val currentArrows = state.ends.getOrNull(state.currentEndIndex) ?: emptyList()

            if (currentArrows.size < state.activeArrowsPerEnd && state.activeEndDbId != null) {
                val newArrow = Arrow(
                    endId = state.activeEndDbId,
                    sequenceOrder = currentArrows.size + 1,
                    scoreValue = score,
                    isXRing = isX,
                    xCoordinate = xCoord,
                    yCoordinate = yCoord
                )

                scoringRepository.insertArrows(listOf(newArrow))

                if (currentArrows.size + 1 == state.activeArrowsPerEnd) {
                    if (canAddNewEnd(state)) {
                        scoringRepository.insertEnd(
                            end = End(sessionId = sessionId, sequenceOrder = state.currentEndIndex + 2),
                        )
                    } else {
                        // --- NEW: Trigger Toast on Append Complete! ---
                        _uiEvent.send(ScoringUiEvent.ShowToast("Round Complete! 🎯"))
                    }
                }
            }
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            scoringRepository.getEndsForSessionStream(sessionId).collect { dbEnds ->
                if (dbEnds.isEmpty()) return@collect

                val mappedEndIds = dbEnds.map { it.id }
                val mappedEnds = dbEnds.map { endWithArrows ->
                    endWithArrows.arrows.map { arrow ->
                        val displayValue = when {
                            arrow.isXRing -> "X"
                            arrow.scoreValue == 0 -> "M"
                            else -> arrow.scoreValue.toString()
                        }
                        ArrowInput(
                            displayValue,
                            arrow.scoreValue,
                            arrow.isXRing,
                            arrow.id,
                            arrow.xCoordinate,
                            arrow.yCoordinate
                        )
                    }
                }

                val allArrows = mappedEnds.flatten()
                val totalScore = allArrows.sumOf { it.score }
                val golds = allArrows.count { it.value == "10" || it.value == "X" }
                val avg = if (allArrows.isNotEmpty()) totalScore.toFloat() / allArrows.size else 0f

                _uiState.update { state ->
                    val newCurrentIndex =
                        if (state.selectedEndIndex != null) state.currentEndIndex else mappedEnds.size - 1

                    // --- Dynamic Multi-Distance Logic ---
                    val activeDistance = getActiveDistanceConfig(state.distances, newCurrentIndex)
                    val distanceText = activeDistance?.let {
                        "${it.distanceValue}${
                            it.distanceUnit.name.take(1).lowercase()
                        }"
                    } ?: "Target"

                    // Keep the canvas dots synced
                    val viewedEndIndex = state.selectedEndIndex ?: newCurrentIndex
                    val hitsForViewedEnd = mappedEnds.getOrNull(viewedEndIndex)?.mapNotNull {
                        if (it.xCoord != null && it.yCoord != null) Offset(
                            it.xCoord,
                            it.yCoord
                        ) else null
                    } ?: emptyList()

                    state.copy(
                        ends = mappedEnds,
                        endDbIds = mappedEndIds,
                        totalScore = totalScore,
                        golds = golds,
                        average = avg,
                        currentEndIndex = newCurrentIndex,
                        activeEndDbId = dbEnds.last().id,
                        currentEndHits = hitsForViewedEnd,

                        // Apply active distance configs
                        activeDistanceText = distanceText,
                        activeArrowsPerEnd = activeDistance?.arrowsPerEnd ?: 6,
                        activeTargetFace = activeDistance?.targetFaceSize ?: TargetFaceSize.CM_122,
                        // Assumes all distances in a round share the same scoring method
                        activeScoringMethod = state.distances.firstOrNull()
                            ?.let { state.activeScoringMethod } ?: "METRIC_10_ZONE"
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
                scoringRepository.deleteArrow(
                    Arrow(
                        id = arrowToDeleteId,
                        endId = state.activeEndDbId!!,
                        sequenceOrder = currentArrows.size,
                        scoreValue = 0
                    )
                )
            } else if (state.currentEndIndex > 0) {
                scoringRepository.deleteEnd(
                    End(
                        id = state.activeEndDbId!!,
                        sessionId = sessionId,
                        sequenceOrder = state.currentEndIndex + 1
                    )
                )
            }
        }
    }

    fun onNextEnd() {
        viewModelScope.launch {
            val state = _uiState.value
            if (canAddNewEnd(state)) {
                scoringRepository.insertEnd(
                    end = End(sessionId = sessionId, sequenceOrder = state.currentEndIndex + 2),
                )
                _uiState.update { it.copy(currentEndHits = emptyList()) }
            }
        }
    }

    class Factory(
        private val sessionId: Long,
        private val scoringRepository: ScoringRepository,
        private val sessionRepository: SessionRepository,
        private val roundRepository: RoundRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScoringViewModel(
                sessionId,
                scoringRepository,
                sessionRepository,
                roundRepository
            ) as T
        }
    }
}