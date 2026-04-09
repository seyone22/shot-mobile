package dev.seyone.shot.presentation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import dev.seyone.shot.presentation.screen.shotscreen.ActiveSessionPagerScreen
import dev.seyone.shot.presentation.screen.shotscreen.SessionListScreen
import dev.seyone.shot.presentation.screen.shotscreen.ShotScreenViewModel

@Composable
fun ShotApp() {
    val navController = rememberSwipeDismissableNavController()
    // Using default viewModel() for now, swap to AppViewModelProvider later for DI
    val viewModel: ShotScreenViewModel = viewModel()

    AppScaffold {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "session_list"
        ) {
            composable("session_list") {
                SessionListScreen(
                    onSessionSelected = { selectedSession ->
                        viewModel.loadSession(selectedSession)
                        navController.navigate("active_session")
                    }
                )
            }
            composable("active_session") {
                ActiveSessionPagerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}