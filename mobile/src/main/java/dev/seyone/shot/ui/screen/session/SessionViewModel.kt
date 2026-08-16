package dev.seyone.shot.ui.screen.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.model.Archer
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.model.BowProfile
import dev.seyone.core.domain.model.Location
import dev.seyone.core.domain.model.Session
import dev.seyone.core.domain.repository.ArcherRepository
import dev.seyone.core.domain.repository.ArrowSetRepository
import dev.seyone.core.domain.repository.BowProfileRepository
import dev.seyone.core.domain.repository.LocationRepository
import dev.seyone.core.domain.repository.RoundRepository
import dev.seyone.core.domain.repository.ScoringRepository
import dev.seyone.core.domain.repository.SessionRepository
import dev.seyone.core.data.repository.SettingsRepository
import dev.seyone.core.data.repository.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
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

data class TruncationWarningData(
    val message: String,
    val onConfirm: () -> Unit
)

class SessionViewModel(
    private val sessionRepository: SessionRepository,
    private val roundRepository: RoundRepository,
    private val scoringRepository: ScoringRepository,
    private val locationRepository: LocationRepository,
    private val bowProfileRepository: BowProfileRepository,
    private val arrowSetRepository: ArrowSetRepository,
    private val archerRepository: ArcherRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val locations: StateFlow<List<Location>> = locationRepository.getAllLocationsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bowProfiles: StateFlow<List<BowProfile>> = bowProfileRepository.getAllBowProfilesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val arrowSets: StateFlow<List<ArrowSet>> = arrowSetRepository.getAllArrowSetsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archers: StateFlow<List<Archer>> = archerRepository.getArchersStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // --- Search & Filter State ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter = _selectedFilter.asStateFlow()

    // --- Bottom Sheet Live Summary State ---
    private val _sessionSummary = MutableStateFlow<SessionSummaryData?>(null)
    val sessionSummary: StateFlow<SessionSummaryData?> = _sessionSummary.asStateFlow()

    private val _truncationWarning = MutableStateFlow<TruncationWarningData?>(null)
    val truncationWarning: StateFlow<TruncationWarningData?> = _truncationWarning.asStateFlow()

    private var summaryJob: Job? = null

    val availableRounds: StateFlow<List<dev.seyone.core.domain.model.Round>> =
        roundRepository.getAllRoundsStream()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSessionList: StateFlow<List<Session>> = combine(
        sessionRepository.getAllSessionsStream(),
        _searchQuery,
        _selectedFilter,
        availableRounds
    ) { sessions, query, filter, rounds ->
        sessions.filter { session ->
            val round = rounds.find { it.id == session.roundId }
            val roundName = round?.name.orEmpty()
            val category = round?.category.orEmpty()

            val matchesSearch = query.isBlank() ||
                    roundName.contains(query, ignoreCase = true) ||
                    session.notes.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Practice" -> session.sessionType == SessionType.PRACTICE
                "Competition" -> session.sessionType == SessionType.COMPETITION
                "WA Outdoor" -> category.contains("WA (Outdoor)", ignoreCase = true)
                "WA Indoor" -> category.contains("WA (Indoor)", ignoreCase = true)
                else -> true // "All"
            }

            matchesSearch && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun loadSessionSummary(sessionId: Long) {
        summaryJob?.cancel()
        summaryJob = viewModelScope.launch {
            scoringRepository.getEndsForSessionStream(sessionId).collect { endsWithArrows ->
                val allArrows = endsWithArrows.flatMap { it.arrows }
                val total = allArrows.sumOf { it.scoreValue }
                val golds = allArrows.count { it.scoreValue == 10 }
                val xs = allArrows.count { it.isXRing }
                val hits = allArrows.count { it.scoreValue > 0 }
                val avg = if (allArrows.isNotEmpty()) total.toFloat() / allArrows.size else 0f

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

    fun clearTruncationWarning() {
        _truncationWarning.value = null
    }

    fun startNewSession(
        roundId: Long, sessionType: SessionType, inputMethod: InputMethod,
        numberOfArchers: Int, arrowsPerEnd: Int, sessionName: String = "",
        bowName: String = "", locationName: String = "", archerName: String = "", arrowName: String = "",
        timestamp: Long = System.currentTimeMillis(),
        onSessionCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val allBows = bowProfileRepository.getAllBowProfilesStream().firstOrNull() ?: emptyList()
            val allLocations = locationRepository.getAllLocationsStream().firstOrNull() ?: emptyList()
            val allArchers = archerRepository.getArchersStream().firstOrNull() ?: emptyList()
            val allArrows = arrowSetRepository.getAllArrowSetsStream().firstOrNull() ?: emptyList()
            val matchingBowId = allBows.find { it.name == bowName }?.id
            val matchingLocationId = allLocations.find { it.name == locationName }?.id
            val matchingArcherId = allArchers.find { it.name == archerName }?.id
            val matchingArrowId = allArrows.find { it.name == arrowName }?.id

            val newSession = Session(
                roundId = roundId, sessionType = sessionType, inputMethod = inputMethod,
                numberOfArchers = numberOfArchers, arrowsPerEnd = arrowsPerEnd,
                notes = sessionName,
                archerId = matchingArcherId, bowId = matchingBowId, arrowId = matchingArrowId, locationId = matchingLocationId,
                timestamp = timestamp
            )
            onSessionCreated(sessionRepository.insertSession(newSession))
        }
    }

    fun updateSessionDetails(
        existingSession: Session,
        newRoundId: Long,
        newSessionType: SessionType,
        newInputMethod: InputMethod,
        newArchers: Int,
        newArrowsPerEnd: Int,
        newSessionName: String,
        newBowName: String = "",
        newLocationName: String = "",
        newArcherName: String = "",
        newArrowName: String = "",
        newTimestamp: Long = existingSession.timestamp,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val newRound = roundRepository.getRoundStream(newRoundId).firstOrNull() ?: return@launch
            val newMaxEnds = if (newRound.distances.isNotEmpty()) newRound.distances.sumOf { it.numberOfEnds } else Int.MAX_VALUE
            val existingEnds = scoringRepository.getEndsForSessionStream(existingSession.id).firstOrNull() ?: emptyList()

            val excessEnds = existingEnds.filter { it.sequenceOrder > newMaxEnds && it.arrows.isNotEmpty() }
            val excessArrows = existingEnds.filter { it.sequenceOrder <= newMaxEnds }
                .flatMap { end -> end.arrows.filter { it.sequenceOrder > newArrowsPerEnd } }

            val willLoseData = excessEnds.isNotEmpty() || excessArrows.isNotEmpty()

            val executeSaveAndClean: () -> Unit = {
                viewModelScope.launch {
                    val allBows = bowProfileRepository.getAllBowProfilesStream().firstOrNull() ?: emptyList()
                    val allLocations = locationRepository.getAllLocationsStream().firstOrNull() ?: emptyList()
                    val allArchers = archerRepository.getArchersStream().firstOrNull() ?: emptyList()
                    val allArrows = arrowSetRepository.getAllArrowSetsStream().firstOrNull() ?: emptyList()
                    val matchingBowId = allBows.find { it.name == newBowName }?.id
                    val matchingLocationId = allLocations.find { it.name == newLocationName }?.id
                    val matchingArcherId = allArchers.find { it.name == newArcherName }?.id
                    val matchingArrowId = allArrows.find { it.name == newArrowName }?.id

                    val updatedSession = existingSession.copy(
                        roundId = newRoundId,
                        sessionType = newSessionType,
                        inputMethod = newInputMethod,
                        numberOfArchers = newArchers,
                        arrowsPerEnd = newArrowsPerEnd,
                        notes = newSessionName,
                        archerId = matchingArcherId,
                        bowId = matchingBowId,
                        arrowId = matchingArrowId,
                        locationId = matchingLocationId,
                        timestamp = newTimestamp
                    )
                    sessionRepository.updateSession(updatedSession)

                    // 1. Delete excess ends beyond new round cap
                    existingEnds.filter { it.sequenceOrder > newMaxEnds }.forEach { endToDelete ->
                        scoringRepository.deleteEnd(endToDelete)
                    }

                    // 2. Delete excess arrows per end beyond new arrowsPerEnd cap
                    existingEnds.filter { it.sequenceOrder <= newMaxEnds }.forEach { end ->
                        end.arrows.filter { it.sequenceOrder > newArrowsPerEnd }.forEach { arrowToDelete ->
                            scoringRepository.deleteArrow(arrowToDelete)
                        }
                    }

                    clearTruncationWarning()
                    onComplete()
                }
            }

            if (willLoseData) {
                val warningMsg = buildString {
                    append("Updating this session configuration will remove recorded data:")
                    if (excessEnds.isNotEmpty()) {
                        append("\n• Ends beyond End $newMaxEnds (Ends ${excessEnds.first().sequenceOrder}–${excessEnds.last().sequenceOrder}) will be deleted.")
                    }
                    if (excessArrows.isNotEmpty()) {
                        append("\n• Arrows beyond $newArrowsPerEnd arrows/end in remaining ends will be trimmed.")
                    }
                    append("\n\nDo you want to proceed?")
                }

                _truncationWarning.value = TruncationWarningData(
                    message = warningMsg,
                    onConfirm = executeSaveAndClean
                )
            } else {
                executeSaveAndClean()
            }
        }
    }

    fun saveLocation(location: Location) {
        viewModelScope.launch {
            if (location.id == 0L) {
                locationRepository.insertLocation(location)
            } else {
                locationRepository.updateLocation(location)
            }
        }
    }

    fun saveBowProfile(profile: BowProfile) {
        viewModelScope.launch {
            if (profile.isDefault) {
                val currentDefaults = bowProfileRepository.getAllBowProfilesStream().firstOrNull()?.filter { it.isDefault }
                currentDefaults?.forEach { oldDefault ->
                    bowProfileRepository.updateBowProfile(oldDefault.copy(isDefault = false))
                }
            }
            if (profile.id == 0L) {
                bowProfileRepository.insertBowProfile(profile)
            } else {
                bowProfileRepository.updateBowProfile(profile)
            }
        }
    }

    fun saveArcher(archer: Archer) {
        viewModelScope.launch {
            if (archer.id == 0L) {
                archerRepository.insertArcher(archer)
            }
        }
    }

    fun saveArrowSet(arrowSet: ArrowSet) {
        viewModelScope.launch {
            if (arrowSet.id == 0L) {
                arrowSetRepository.insertArrowSet(arrowSet)
            } else {
                arrowSetRepository.updateArrowSet(arrowSet)
            }
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch { sessionRepository.deleteSession(session) }
    }

    fun updateSessionNotes(session: Session, newNotes: String) {
        viewModelScope.launch {
            sessionRepository.updateSession(session.copy(notes = newNotes))
        }
    }

    fun getSessionStats(sessionId: Long): Flow<Pair<Int, Float>> {
        return scoringRepository.getEndsForSessionStream(sessionId).map { endsWithArrows ->
            val allArrows = endsWithArrows.flatMap { it.arrows }
            val total = allArrows.sumOf { it.scoreValue }
            val avg = if (allArrows.isNotEmpty()) total.toFloat() / allArrows.size else 0f

            Pair(total, avg)
        }
    }

    class Factory(
        private val sessionRepository: SessionRepository,
        private val roundRepository: RoundRepository,
        private val scoringRepository: ScoringRepository,
        private val locationRepository: LocationRepository,
        private val bowProfileRepository: BowProfileRepository,
        private val arrowSetRepository: ArrowSetRepository,
        private val archerRepository: ArcherRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SessionViewModel(
                sessionRepository,
                roundRepository,
                scoringRepository,
                locationRepository,
                bowProfileRepository,
                arrowSetRepository,
                archerRepository,
                settingsRepository
            ) as T
        }
    }
}