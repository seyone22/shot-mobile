package com.seyone22.shot.ui.screen.more.bow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.shot.data.domain.repository.BowComponentRepository
import com.seyone22.shot.data.domain.repository.BowProfileRepository
import com.seyone22.shot.data.local.entity.BowComponentEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BowDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val bowProfileRepository: BowProfileRepository,
    private val componentRepository: BowComponentRepository,
) : ViewModel() {
    // 1. Extract the ID directly from the navigation route
    private val bowId: Long = checkNotNull(savedStateHandle["bowId"])

    val bowProfile = bowProfileRepository.getBowProfileStream(bowId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val components = componentRepository.getComponentsForBowStream(bowId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun saveComponent(component: BowComponentEntity) {
        viewModelScope.launch {
            if (component.id == 0L) componentRepository.insert(component)
            else componentRepository.update(component)
        }
    }

    fun deleteBowProfile(id: Long) {
        viewModelScope.launch {
            bowProfileRepository.deleteBowProfileById(id)
            // If your database doesn't cascade deletes, you might also
            // need to delete associated components here:
            // repository.deleteComponentsByBowId(id)
        }
    }

    fun deleteComponent(component: BowComponentEntity) {
        viewModelScope.launch { componentRepository.delete(component) }
    }
}