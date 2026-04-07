package dev.seyone.shot.ui.screen.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.seyone.shot.data.domain.repository.BowProfileRepository
import dev.seyone.shot.data.local.entity.BowProfileEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BowEquipmentViewModel(
    private val bowProfileRepository: BowProfileRepository
) : ViewModel() {

    // Automatically observe all bows from the database
    val bowProfiles: StateFlow<List<BowProfileEntity>> = bowProfileRepository.getAllBowProfilesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun saveBowProfile(profile: BowProfileEntity) {
        viewModelScope.launch {
            // If this new/updated bow is set as default, we must un-default the others first
            if (profile.isDefault) {
                val currentBows = bowProfiles.value
                currentBows.filter { it.isDefault && it.id != profile.id }.forEach { oldDefault ->
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

    fun deleteBowProfile(profile: BowProfileEntity) {
        viewModelScope.launch {
            bowProfileRepository.deleteBowProfile(profile)
        }
    }
}