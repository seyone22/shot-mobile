package dev.seyone22.shot.presentation

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.seyone22.shot.presentation.screen.shotscreen.ShotScreenViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Shot screen ViewModel
        initializer {
            ShotScreenViewModel()
        }
    }
}
