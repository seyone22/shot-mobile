package dev.seyone.shot.ui.screen.more.arrow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.repository.ArrowSetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArrowEquipmentViewModel(
    private val arrowSetRepository: ArrowSetRepository
) : ViewModel() {

    // Automatically observe the database and emit changes to the UI
    val arrowSets: StateFlow<List<ArrowSet>> = arrowSetRepository.getAllArrowSetsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveArrowSet(arrowSet: ArrowSet) {
        viewModelScope.launch {
            if (arrowSet.id == 0L) {
                arrowSetRepository.insertArrowSet(arrowSet)
            } else {
                arrowSetRepository.updateArrowSet(arrowSet)
            }
        }
    }

    fun deleteArrowSet(arrowSet: ArrowSet) {
        viewModelScope.launch {
            arrowSetRepository.deleteArrowSet(arrowSet)
        }
    }
}