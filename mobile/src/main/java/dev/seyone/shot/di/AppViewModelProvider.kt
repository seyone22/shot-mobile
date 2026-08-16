// AppViewModelProvider.kt
package dev.seyone.shot.di // Or wherever you keep your app-level UI config

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.seyone.shot.ShotApplication
import dev.seyone.shot.ui.screen.more.arrow.ArrowEquipmentViewModel
import dev.seyone.shot.ui.screen.more.bow.BowEquipmentViewModel
import dev.seyone.shot.ui.screen.more.bow.BowDetailViewModel
import dev.seyone.shot.ui.screen.more.location.LocationViewModel
import dev.seyone.shot.ui.screen.more.sight.SightMarksViewModel
import dev.seyone.shot.ui.screen.statistics.StatisticsViewModel
import dev.seyone.shot.ui.screen.session.SessionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for SessionViewModel
        initializer {
            SessionViewModel(
                sessionRepository = shotApplication().container.sessionRepository,
                roundRepository = shotApplication().container.roundRepository,
                scoringRepository = shotApplication().container.scoringRepository,
                locationRepository = shotApplication().container.locationRepository,
                bowProfileRepository = shotApplication().container.bowProfileRepository,
                arrowSetRepository = shotApplication().container.arrowSetRepository,
                archerRepository = shotApplication().container.archerRepository,
                settingsRepository = shotApplication().container.settingsRepository
            )
        }
        initializer {
            StatisticsViewModel(
                sessionRepository = shotApplication().container.sessionRepository,
                scoringRepository = shotApplication().container.scoringRepository,
                roundRepository = shotApplication().container.roundRepository
            )
        }
        initializer {
            BowEquipmentViewModel(
                bowProfileRepository = shotApplication().container.bowProfileRepository
            )
        }
        initializer {
            BowDetailViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                bowProfileRepository = shotApplication().container.bowProfileRepository,
                componentRepository = shotApplication().container.bowComponentRepository
            )
        }
        initializer {
            ArrowEquipmentViewModel(
                arrowSetRepository = shotApplication().container.arrowSetRepository
            )
        }
        initializer {
            LocationViewModel(
                locationRepository = shotApplication().container.locationRepository
            )
        }
        initializer {
            SightMarksViewModel(
                sightMarkRepository = shotApplication().container.sightMarkRepository,
                bowProfileRepository = shotApplication().container.bowProfileRepository,
                arrowSetRepository = shotApplication().container.arrowSetRepository
            )
        }
    }
}

// Helper extension function to get the application object
fun CreationExtras.shotApplication(): ShotApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShotApplication)