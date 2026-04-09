package dev.seyone.shot.ui.screen.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigateToArchers: () -> Unit,
    onNavigateToBows: () -> Unit,
    onNavigateToArrows: () -> Unit,
    onNavigateToLocations: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("More") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Group: Management ---
            MoreItem(
                label = "Archers",
                icon = Icons.Outlined.People,
                onClick = onNavigateToArchers
            )
            MoreItem(
                label = "Bow Profiles",
                icon = Icons.Outlined.Architecture, // Or custom bow icon
                onClick = onNavigateToBows
            )
            MoreItem(
                label = "Arrow Sets",
                icon = Icons.Outlined.Straighten,
                onClick = onNavigateToArrows
            )
            MoreItem(
                label = "Locations",
                icon = Icons.Outlined.Place,
                onClick = onNavigateToLocations
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --- Group: Data & App ---
            MoreItem(
                label = "Backup and Restore",
                icon = Icons.Outlined.CloudUpload,
                onClick = { /* TODO */ }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


            MoreItem(
                label = "General Settings",
                icon = Icons.Outlined.Settings,
                onClick = { /* TODO */ }
            )
            MoreItem(
                label = "About",
                icon = Icons.Outlined.Info,
                onClick = onNavigateToAbout // Add this callback to your MoreScreen params
            )
            MoreItem(
                label = "Help & Feedback",
                icon = Icons.Outlined.HelpOutline,
                onClick = { /* TODO */ }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun MoreItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    subtitle: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}