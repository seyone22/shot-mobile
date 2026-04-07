package dev.seyone.shot.ui.screen.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.shot.data.domain.repository.ArcherRepository
import dev.seyone.shot.data.local.entity.AgeGroup
import dev.seyone.shot.data.local.entity.ArcherEntity
import dev.seyone.shot.data.local.entity.Gender
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MoreViewModel(
    private val archerRepository: ArcherRepository
) : ViewModel() {

    val archers: StateFlow<List<ArcherEntity>> = archerRepository.getArchersStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addArcher(name: String, club: String?, gender: Gender, ageGroup: AgeGroup) {
        viewModelScope.launch {
            val newArcher = ArcherEntity(
                name = name,
                clubName = if (club.isNullOrBlank()) null else club,
                gender = gender,
                ageGroup = ageGroup
            )
            archerRepository.insertArcher(newArcher)
        }
    }

    fun deleteArcher(archer: ArcherEntity) {
        viewModelScope.launch {
            archerRepository.deleteArcher(archer)
        }
    }

    // Factory for DI
    class Factory(private val archerRepository: ArcherRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MoreViewModel(archerRepository) as T
        }
    }
}