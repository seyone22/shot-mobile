package dev.seyone.shot.ui.screen.more.about


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


import androidx.compose.ui.res.stringResource
import com.seyone22.shot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What's New") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                VersionHeader(
                    version = stringResource(R.string.app_version_name),
                    name = stringResource(R.string.app_release_title)
                )
                BulletPoint("Sight Marks & Trajectory Interpolation", "Comprehensive sight mark management with quadratic curve fitting, draw weight scaling, and range lookup card.")
                BulletPoint("Android Homescreen Widgets", "Quick Start and Live Archery Stats phone widgets with 1-tap app launch.")
                BulletPoint("App Launcher Shortcuts", "Long-press app icon shortcuts for Quick Start, Sight Marks, and Data Backup.")
                BulletPoint("Default Round Settings", "Set your preferred competition round pre-selected for new sessions.")
                BulletPoint("Core Scoring Engine", "Support for WA, Archery GB, and NFAA standard and custom rounds.")
            }
        }
    }
}

@Composable
private fun VersionHeader(version: String, name: String) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(version, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BulletPoint(title: String, description: String) {
    Row(Modifier.padding(vertical = 4.dp)) {
        Text("• ", fontWeight = FontWeight.Bold)
        Column {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}