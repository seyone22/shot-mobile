package com.seyone22.shot.ui.screens.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.seyone22.shot.data.domain.repository.RoundRepository
import com.seyone22.shot.data.domain.repository.ScoringRepository
import com.seyone22.shot.data.domain.repository.SessionRepository
import com.seyone22.shot.data.local.entity.InputMethod
import com.seyone22.shot.data.local.entity.RoundWithDistances
import com.seyone22.shot.data.local.entity.SessionEntity
import com.seyone22.shot.data.local.entity.SessionType
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- New Data Class for the Bottom Sheet ---
data class SessionSummaryData(
    val totalScore: Int = 0,
    val average: Float = 0f,
    val golds: Int = 0,
    val xs: Int = 0,
    val hits: Int = 0,
    val totalArrowsShot: Int = 0,
    val endScores: List<Int> = emptyList() // Used for the line chart
)

class SessionViewModel(
    private val sessionRepository: SessionRepository,
    private val roundRepository: RoundRepository,
    private val scoringRepository: ScoringRepository // <-- ADDED
) : ViewModel() {

    val sessionList: StateFlow<List<SessionEntity>> = sessionRepository.getAllSessionsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val availableRounds: StateFlow<List<RoundWithDistances>> = roundRepository.getAllRoundsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // --- Summary State ---
    private val _sessionSummary = MutableStateFlow<SessionSummaryData?>(null)
    val sessionSummary: StateFlow<SessionSummaryData?> = _sessionSummary.asStateFlow()

    private var summaryJob: Job? = null

    // Call this when a card is clicked
    fun loadSessionSummary(sessionId: Long) {
        summaryJob?.cancel() // Cancel previous observation if user taps fast
        summaryJob = viewModelScope.launch {
            scoringRepository.getEndsWithArrowsForSession(sessionId).collect { endsWithArrows ->
                val allArrows = endsWithArrows.flatMap { it.arrows }

                val total = allArrows.sumOf { it.scoreValue }
                val golds = allArrows.count { it.scoreValue == 10 }
                val xs = allArrows.count { it.isXRing }
                val hits = allArrows.count { it.scoreValue > 0 }
                val avg = if (allArrows.isNotEmpty()) total.toFloat() / allArrows.size else 0f

                // Calculate total score per end for the chart
                val chartData = endsWithArrows.map { end ->
                    end.arrows.sumOf { it.scoreValue }
                }

                _sessionSummary.value = SessionSummaryData(
                    totalScore = total,
                    average = avg,
                    golds = golds,
                    xs = xs,
                    hits = hits,
                    totalArrowsShot = allArrows.size,
                    endScores = chartData
                )
            }
        }
    }

    fun clearSessionSummary() {
        summaryJob?.cancel()
        _sessionSummary.value = null
    }

    fun startNewSession(
        roundId: Long, sessionType: SessionType, inputMethod: InputMethod,
        numberOfArchers: Int, arrowsPerEnd: Int, onSessionCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val newSession = SessionEntity(
                roundId = roundId, sessionType = sessionType, inputMethod = inputMethod,
                numberOfArchers = numberOfArchers, arrowsPerEnd = arrowsPerEnd,
                bowId = null, arrowId = null, locationId = null
            )
            onSessionCreated(sessionRepository.insertSession(newSession))
        }
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch { sessionRepository.deleteSession(session) }
    }

    fun updateSessionNotes(session: SessionEntity, newNotes: String) {
        viewModelScope.launch {
            // Copy the existing session with the new notes string and update the DB
            sessionRepository.updateSession(session.copy(notes = newNotes))
        }
    }

    // UPDATE FACTORY TO ACCEPT ScoringRepository
    class Factory(
        private val sessionRepository: SessionRepository,
        private val roundRepository: RoundRepository,
        private val scoringRepository: ScoringRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionViewModel(sessionRepository, roundRepository, scoringRepository) as T
        }
    }
}