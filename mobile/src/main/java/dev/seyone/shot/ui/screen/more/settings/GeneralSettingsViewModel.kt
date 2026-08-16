package dev.seyone.shot.ui.screen.more.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.core.data.repository.AppThemeMode
import dev.seyone.core.data.repository.ArrowSortOrder
import dev.seyone.core.data.repository.SettingsRepository
import dev.seyone.core.data.repository.UserSettings
import dev.seyone.core.domain.model.Round
import dev.seyone.core.domain.repository.RoundRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class GeneralSettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val roundRepository: RoundRepository
) : ViewModel() {

    val settingsState: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val availableRounds: StateFlow<List<Round>> = roundRepository.getAllRoundsStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThemeMode(mode: AppThemeMode) {
        settingsRepository.setThemeMode(mode)
    }

    fun setArrowSortOrder(order: ArrowSortOrder) {
        settingsRepository.setArrowSortOrder(order)
    }

    fun setDefaultRoundId(roundId: Long) {
        settingsRepository.setDefaultRoundId(roundId)
    }

    class Factory(
        private val settingsRepository: SettingsRepository,
        private val roundRepository: RoundRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GeneralSettingsViewModel(settingsRepository, roundRepository) as T
        }
    }
}
