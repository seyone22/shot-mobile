package com.seyone22.shot

import com.seyone22.shot.ui.screen.more.ArchersListScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seyone22.shot.di.AppContainer
import com.seyone22.shot.ui.screen.more.AboutScreen
import com.seyone22.shot.ui.screen.more.MoreScreen
import com.seyone22.shot.ui.screen.scoring.ScoringScreen
import com.seyone22.shot.ui.screen.session.SessionScreen
import com.seyone22.shot.ui.theme.ShotTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the manual DI container from the Application class
        val appContainer = (application as ShotApplication).container

        enableEdgeToEdge()
        setContent {
            ShotTheme {
                ShotApp(appContainer = appContainer)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun ShotApp(
    appContainer: AppContainer, navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 1. Check if the user is currently on a main tab
    val isTopLevel = TopLevelDestination.entries.any { it.route == currentDestination?.route }

    // 2. Dynamically hide the navigation suite if they are scoring
    val layoutType = if (isTopLevel) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    } else {
        NavigationSuiteType.None
    }

    NavigationSuiteScaffold(
        layoutType = layoutType, // Apply the dynamic layout type here
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentDestination?.hierarchy?.any {
                    it.route == destination.route
                } == true

                item(
                    icon = {
                        Icon(
                            // --- DYNAMIC ICON LOGIC ---
                            imageVector = if (isSelected) {
                                destination.selectedIcon
                            } else {
                                destination.unselectedIcon
                            },
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }) {
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.SESSION.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(TopLevelDestination.SESSION.route) {
                SessionScreen(
                    sessionRepository = appContainer.sessionRepository,
                    roundRepository = appContainer.roundRepository,
                    scoringRepository = appContainer.scoringRepository,
                    onNavigateToScoring = { sessionId ->
                        navController.navigate("scoring_stub/$sessionId")
                    })
            }

            composable("about") {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable("scoring_stub/{sessionId}") { backStackEntry ->
                val sessionId =
                    backStackEntry.arguments?.getString("sessionId")?.toLongOrNull() ?: -1L

                ScoringScreen(
                    sessionId = sessionId,
                    scoringRepository = appContainer.scoringRepository,
                    sessionRepository = appContainer.sessionRepository,
                    roundRepository = appContainer.roundRepository, // <-- Pass it here!
                    onNavigateBack = { navController.popBackStack() })
            }

            composable(TopLevelDestination.SOCIAL.route) {
                PlaceholderScreen("Social Screen")
            }

            composable(TopLevelDestination.STATISTICS.route) {
                PlaceholderScreen("Statistics Screen")
            }

            composable(TopLevelDestination.MORE.route) {
                MoreScreen(
                    onNavigateToArchers = { navController.navigate("manage_archers") },
                    onNavigateToBows = { navController.navigate("manage_bows") },
                    onNavigateToArrows = { navController.navigate("manage_arrows") },
                    onNavigateToLocations = { navController.navigate("manage_locations") },
                    onNavigateToAbout = { navController.navigate("about") }
                )
            }

// Sub-screens
            composable("manage_archers") { ArchersListScreen(onNavigateBack = { navController.popBackStack() }, archerRepository = appContainer.archerRepository) }
// ... and so on
            composable("about") {
                AboutScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

enum class TopLevelDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    SESSION(
        label = "Session",
        selectedIcon = Icons.Filled.TrackChanges, // Better "Target" feel
        unselectedIcon = Icons.Outlined.TrackChanges,
        route = "session"
    ),
    SOCIAL(
        label = "Social",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People,
        route = "social"
    ),
    STATISTICS(
        label = "Statistics",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart,
        route = "statistics"
    ),
    MORE(
        label = "More",
        selectedIcon = Icons.Filled.MoreHoriz,
        unselectedIcon = Icons.Outlined.MoreHoriz,
        route = "more"
    )
}

// A temporary composable to visually verify navigation is working
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}