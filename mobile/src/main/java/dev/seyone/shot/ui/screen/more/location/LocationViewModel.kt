package dev.seyone.shot.ui.screen.more.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.seyone.core.domain.model.Location
import dev.seyone.core.domain.repository.LocationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LocationViewModel(
    private val locationRepository: LocationRepository
) : ViewModel() {

    val locations: StateFlow<List<Location>> = locationRepository.getAllLocationsStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun saveLocation(location: Location) {
        viewModelScope.launch {
            if (location.id == 0L) {
                locationRepository.insertLocation(location)
            } else {
                locationRepository.updateLocation(location)
            }
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            locationRepository.deleteLocation(location)
        }
    }
}