package dev.seyone.shot.presentation

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.seyone.shot.presentation.screen.shotscreen.ShotScreenViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Shot screen ViewModel
        initializer {
            ShotScreenViewModel()
        }
    }
}
