package dev.seyone.shot.ui.screen.more.sight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.model.BowProfile
import dev.seyone.core.domain.model.SightMark
import dev.seyone.core.domain.repository.ArrowSetRepository
import dev.seyone.core.domain.repository.BowProfileRepository
import dev.seyone.core.domain.repository.SightMarkRepository
import dev.seyone.core.domain.util.SightMarkCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SightMarksViewModel(
    private val sightMarkRepository: SightMarkRepository,
    private val bowProfileRepository: BowProfileRepository,
    private val arrowSetRepository: ArrowSetRepository
) : ViewModel() {

    val bows: StateFlow<List<BowProfile>> = bowProfileRepository.getAllBowProfilesStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val arrowSets: StateFlow<List<ArrowSet>> = arrowSetRepository.getAllArrowSetsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedBowId = MutableStateFlow<Long?>(null)
    val selectedBowId: StateFlow<Long?> = _selectedBowId.asStateFlow()

    private val _selectedArrowId = MutableStateFlow<Long?>(null)
    val selectedArrowId: StateFlow<Long?> = _selectedArrowId.asStateFlow()

    private val _customDistanceInput = MutableStateFlow("40")
    val customDistanceInput: StateFlow<String> = _customDistanceInput.asStateFlow()

    val sightMarks: StateFlow<List<SightMark>> = combine(
        _selectedBowId,
        _selectedArrowId
    ) { bowId, arrowId -> Pair(bowId, arrowId) }
        .flatMapLatest { (bowId, arrowId) ->
            if (bowId != null) {
                if (arrowId != null) {
                    sightMarkRepository.getSightMarksForBowAndArrowStream(bowId, arrowId)
                } else {
                    sightMarkRepository.getSightMarksForBowStream(bowId)
                }
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rangeCard: StateFlow<List<SightMark>> = sightMarks.flatMapLatest { marks ->
        val card = SightMarkCalculator.generateRangeCard(marks)
        flowOf(card)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customPredictedMark: StateFlow<Float?> = combine(
        sightMarks,
        _customDistanceInput
    ) { marks, distanceStr ->
        val dist = distanceStr.toFloatOrNull() ?: return@combine null
        val coeffs = SightMarkCalculator.calculateCoefficients(marks) ?: return@combine null
        SightMarkCalculator.predictElevation(coeffs, dist)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            bows.collect { list ->
                if (_selectedBowId.value == null && list.isNotEmpty()) {
                    _selectedBowId.value = list.find { it.isDefault }?.id ?: list.first().id
                }
            }
        }
    }

    fun selectBow(bowId: Long) {
        _selectedBowId.value = bowId
    }

    fun selectArrow(arrowId: Long?) {
        _selectedArrowId.value = arrowId
    }

    fun setCustomDistanceInput(input: String) {
        _customDistanceInput.value = input
    }

    fun saveSightMark(
        distanceValue: Float,
        distanceUnit: DistanceUnit,
        elevationMark: Float,
        windageMark: Float?,
        notes: String
    ) {
        val currentBowId = _selectedBowId.value ?: return
        val activeBow = bows.value.find { it.id == currentBowId }

        val mark = SightMark(
            bowProfileId = currentBowId,
            arrowSetId = _selectedArrowId.value,
            drawWeightLbs = activeBow?.drawWeight,
            distanceValue = distanceValue,
            distanceUnit = distanceUnit,
            elevationMark = elevationMark,
            windageMark = windageMark,
            notes = notes
        )

        viewModelScope.launch {
            sightMarkRepository.insertSightMark(mark)
        }
    }

    fun deleteSightMark(sightMark: SightMark) {
        viewModelScope.launch {
            sightMarkRepository.deleteSightMark(sightMark)
        }
    }

    class Factory(
        private val sightMarkRepository: SightMarkRepository,
        private val bowProfileRepository: BowProfileRepository,
        private val arrowSetRepository: ArrowSetRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SightMarksViewModel(
                sightMarkRepository,
                bowProfileRepository,
                arrowSetRepository
            ) as T
        }
    }
}
