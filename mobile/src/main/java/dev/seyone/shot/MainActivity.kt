package dev.seyone.shot

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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.seyone.shot.ShotApplication
import dev.seyone.shot.di.AppContainer
import dev.seyone.shot.ui.screen.more.AboutScreen
import dev.seyone.shot.ui.screen.more.ArchersListScreen
import dev.seyone.shot.ui.screen.more.BowEquipmentScreen
import dev.seyone.shot.ui.screen.more.MoreScreen
import dev.seyone.shot.ui.screen.more.bow.BowDetailScreen
import dev.seyone.shot.ui.screen.scoring.ScoringScreen
import dev.seyone.shot.ui.screen.session.SessionScreen
import dev.seyone.shot.ui.screen.statistics.StatisticsScreen
import dev.seyone.shot.ui.theme.ShotTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Logic to hide BottomBar/NavRail on sub-screens
    val isTopLevel = TopLevelDestination.entries.any { it.route == currentDestination?.route }
    val layoutType = if (isTopLevel) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    } else {
        NavigationSuiteType.None
    }

    NavigationSuiteScaffold(
        layoutType = layoutType,
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { destination ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                item(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.SESSION.route,
            modifier = Modifier.fillMaxSize()
        ) {
            // --- TOP LEVEL DESTINATIONS ---
            composable(TopLevelDestination.SESSION.route) {
                SessionScreen(onNavigateToScoring = { id -> navController.navigate("scoring/$id") })
            }

            composable(TopLevelDestination.SOCIAL.route) {
                PlaceholderScreen("Social Feed")
            }

            composable(TopLevelDestination.STATISTICS.route) {
                StatisticsScreen()
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

            // --- SCORING FLOW ---
            composable(
                route = "scoring/{sessionId}",
                arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
                ScoringScreen(
                    sessionId = sessionId,
                    scoringRepository = appContainer.scoringRepository,
                    sessionRepository = appContainer.sessionRepository,
                    roundRepository = appContainer.roundRepository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // --- EQUIPMENT & MANAGEMENT SUB-SCREENS ---
            composable("manage_bows") {
                BowEquipmentScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToBowDetail = { id -> navController.navigate("bow_detail/$id") }
                )
            }

            composable(
                route = "bow_detail/{bowId}",
                arguments = listOf(navArgument("bowId") { type = NavType.LongType })
            ) { backStackEntry ->
                val bowId = backStackEntry.arguments?.getLong("bowId") ?: return@composable
                BowDetailScreen(
                    bowId = bowId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditBow = { id ->
                        // Logic for navigating to an Edit Screen would go here
                        navController.navigate("manage_bows")
                    }
                )
            }

            composable("manage_archers") {
                ArchersListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    archerRepository = appContainer.archerRepository
                )
            }

            composable("manage_arrows") { PlaceholderScreen("Manage Arrows") }
            composable("manage_locations") { PlaceholderScreen("Manage Locations") }

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
    SESSION("Session", Icons.Filled.TrackChanges, Icons.Outlined.TrackChanges, "session"),
    SOCIAL("Social", Icons.Filled.People, Icons.Outlined.People, "social"),
    STATISTICS("Statistics", Icons.Filled.BarChart, Icons.Outlined.BarChart, "statistics"),
    MORE("More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz, "more")
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}