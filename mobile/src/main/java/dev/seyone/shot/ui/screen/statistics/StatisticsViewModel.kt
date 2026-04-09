package dev.seyone.shot.ui.screen.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.seyone.core.domain.model.Round
import dev.seyone.core.domain.repository.RoundRepository
import dev.seyone.core.domain.repository.ScoringRepository
import dev.seyone.core.domain.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class TimeRange(val displayName: String) {
    ALL_TIME("All Time"), TODAY("Today"), THIS_WEEK("This Week"), THIS_MONTH("This Month"), THIS_YEAR(
        "This Year"
    ),
    CUSTOM("Custom") // <-- Added Custom
}

data class DailyStat(
    val dateMs: Long, val totalArrows: Int, val averageScore: Float
)

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val selectedTimeRange: TimeRange = TimeRange.THIS_MONTH,
    val selectedRoundId: Long? = null,
    val availableRounds: List<Round> = emptyList(),

    // Core Summaries
    val totalArrowsShot: Int = 0,
    val overallAverage: Float = 0f,
    val totalSessions: Int = 0,

    // Advanced Summaries
    val goldsCount: Int = 0,    // 10s and Xs
    val hitsCount: Int = 0,     // Anything > 0
    val averageArrowsPerSession: Float = 0f,

    val scoreDistribution: Map<String, Int> = emptyMap(),
    val dailyStats: List<DailyStat> = emptyList(),

    // Custom Date Range State
    val customDateRange: Pair<Long, Long>? = null
)

class StatisticsViewModel(
    private val sessionRepository: SessionRepository,
    private val roundRepository: RoundRepository,
    private val scoringRepository: ScoringRepository
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRange.THIS_MONTH)
    private val _selectedRoundId = MutableStateFlow<Long?>(null)
    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
    }

    fun setRoundFilter(roundId: Long?) {
        _selectedRoundId.value = roundId
    }

    fun setCustomDateRange(start: Long, end: Long) {
        // Add 86,399,999 ms to the end date to ensure it covers the very end of the selected day
        _customDateRange.value = Pair(start, end + 86399999L)
        _selectedTimeRange.value = TimeRange.CUSTOM
    }

    private val allSessions = sessionRepository.getAllSessionsStream()
    private val allRounds = roundRepository.getAllRoundsStream()

    val uiState: StateFlow<StatisticsUiState> = combine(
        allSessions, allRounds, _selectedTimeRange, _selectedRoundId, _customDateRange
    ) { sessions, rounds, timeRange, roundId, customDates ->

        // 1. Filter Sessions by Time and Round
        val (startDate, endDate) = getStartAndEndTimes(timeRange, customDates)

        val filteredSessions = sessions.filter { session ->
            val matchesTime = session.timestamp in startDate..endDate
            val matchesRound = roundId == null || session.roundId == roundId
            matchesTime && matchesRound
        }

        var totalArrows = 0
        var totalScore = 0
        var golds = 0
        var hits = 0
        val distribution = mutableMapOf<String, Int>().withDefault { 0 }
        val dailyScoresMap = mutableMapOf<Long, MutableList<Int>>()

        // 2. Fetch Arrows for these sessions and group them
        for (session in filteredSessions) {
            val sessionArrows = scoringRepository.getAllArrowsForSessionSync(session.id)
            val dayMs = truncateToDay(session.timestamp)
            val dailyScores = dailyScoresMap.getOrPut(dayMs) { mutableListOf() }

            for (arrow in sessionArrows) {
                totalArrows++
                totalScore += arrow.scoreValue
                dailyScores.add(arrow.scoreValue)

                if (arrow.scoreValue == 10 || arrow.isXRing) golds++
                if (arrow.scoreValue > 0) hits++

                val label =
                    if (arrow.isXRing) "X" else if (arrow.scoreValue == 0) "M" else arrow.scoreValue.toString()
                distribution[label] = distribution.getValue(label) + 1
            }
        }

        // 3. Calculate Averages
        val overallAvg = if (totalArrows > 0) totalScore.toFloat() / totalArrows else 0f
        val avgArrowsPerSession =
            if (filteredSessions.isNotEmpty()) totalArrows.toFloat() / filteredSessions.size else 0f

        // 4. Calculate Daily Stats for Charts
        val groupedByDay = dailyScoresMap.map { (dayMs, scoresOnDay) ->
            DailyStat(
                dateMs = dayMs,
                totalArrows = scoresOnDay.size,
                averageScore = if (scoresOnDay.isNotEmpty()) scoresOnDay.sum()
                    .toFloat() / scoresOnDay.size else 0f
            )
        }.sortedBy { it.dateMs }

        StatisticsUiState(
            isLoading = false,
            selectedTimeRange = timeRange,
            selectedRoundId = roundId,
            availableRounds = rounds,
            totalArrowsShot = totalArrows,
            overallAverage = overallAvg,
            totalSessions = filteredSessions.size,
            goldsCount = golds,
            hitsCount = hits,
            averageArrowsPerSession = avgArrowsPerSession,
            scoreDistribution = distribution,
            dailyStats = groupedByDay,
            customDateRange = customDates
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())

    // --- Helper Math ---
    private fun getStartAndEndTimes(
        range: TimeRange, customDates: Pair<Long, Long>?
    ): Pair<Long, Long> {
        if (range == TimeRange.CUSTOM && customDates != null) {
            return customDates
        }

        val startCal = Calendar.getInstance()
        val endCal = Calendar.getInstance() // Now

        when (range) {
            TimeRange.TODAY -> startCal.set(Calendar.HOUR_OF_DAY, 0)
            TimeRange.THIS_WEEK -> startCal.set(Calendar.DAY_OF_WEEK, startCal.firstDayOfWeek)
            TimeRange.THIS_MONTH -> startCal.set(Calendar.DAY_OF_MONTH, 1)
            TimeRange.THIS_YEAR -> startCal.set(Calendar.DAY_OF_YEAR, 1)
            TimeRange.ALL_TIME -> return Pair(0L, Long.MAX_VALUE)
            TimeRange.CUSTOM -> return Pair(0L, Long.MAX_VALUE) // Fallback
        }

        startCal.set(Calendar.MINUTE, 0); startCal.set(
            Calendar.SECOND, 0
        ); startCal.set(Calendar.MILLISECOND, 0)
        return Pair(startCal.timeInMillis, endCal.timeInMillis)
    }

    private fun truncateToDay(timestamp: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}