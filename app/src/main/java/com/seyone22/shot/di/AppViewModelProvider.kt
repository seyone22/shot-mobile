// AppViewModelProvider.kt
package com.seyone22.shot.di // Or wherever you keep your app-level UI config

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.seyone22.shot.ShotApplication
import com.seyone22.shot.ui.screen.more.BowEquipmentViewModel
import com.seyone22.shot.ui.screen.more.bow.BowDetailViewModel
import com.seyone22.shot.ui.screen.statistics.StatisticsViewModel
import com.seyone22.shot.ui.screens.session.SessionViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for SessionViewModel
        initializer {
            SessionViewModel(
                sessionRepository = shotApplication().container.sessionRepository,
                roundRepository = shotApplication().container.roundRepository,
                scoringRepository = shotApplication().container.scoringRepository
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
    }
}

// Helper extension function to get the application object
fun CreationExtras.shotApplication(): ShotApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ShotApplication)