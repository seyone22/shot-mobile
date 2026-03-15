package com.seyone22.shot.ui.screen.more

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Hero Section: Logo & Name ---
            Spacer(modifier = Modifier.height(48.dp))

            // Placeholder for your app logo "み" style
            Text(
                text = "🏹", // You can replace this with a local Painter/Logo
                fontSize = 80.sp
            )

            Text(
                text = "Shot",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- Version Info ---
            AboutPreferenceItem(
                title = "Version",
                subtitle = "Stable 0.1.0 (March 15, 2026)",
                onClick = { /* Copy version to clipboard? */ }
            )

            AboutPreferenceItem(
                title = "Check for updates",
                onClick = { /* Trigger update check */ }
            )

            AboutPreferenceItem(
                title = "What's new",
                onClick = { /* Open changelog */ }
            )

            AboutPreferenceItem(
                title = "Open source licenses",
                onClick = { /* Show licenses */ }
            )

            AboutPreferenceItem(
                title = "Privacy policy",
                onClick = { /* Open link */ }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Social Links Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Using standard Icons as placeholders for Discord, GitHub, etc.
                SocialIconButton(Icons.Default.Language) // Website
                SocialIconButton(Icons.Default.BugReport) // GitHub/Issues
                SocialIconButton(Icons.Default.Policy) // Twitter/X or Discord
            }
        }
    }
}

@Composable
fun AboutPreferenceItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
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

@Composable
fun SocialIconButton(icon: ImageVector) {
    IconButton(onClick = { /* Open External Link */ }) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}