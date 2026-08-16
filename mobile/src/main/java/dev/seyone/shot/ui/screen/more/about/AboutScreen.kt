package dev.seyone.shot.ui.screen.more.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.outlined.TrackChanges
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.seyone22.shot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWhatNew: () -> Unit, // Add this to your NavHost
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

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
            Spacer(modifier = Modifier.height(48.dp))

            // Hero Section
            Icon(
                imageVector = Icons.Outlined.TrackChanges,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Shot Mobile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- Version & Updates ---
            AboutPreferenceItem(
                title = "Version",
                subtitle = stringResource(R.string.app_version_full),
                onClick = { /* Could copy to clipboard */ }
            )

            AboutPreferenceItem(
                title = "Check for updates",
                subtitle = "Visit GitHub for the latest release",
                onClick = { uriHandler.openUri("https://github.com/seyone22/shot-mobile/releases") }
            )

            AboutPreferenceItem(
                title = "What's new",
                subtitle = "View recent patch notes and features",
                onClick = onNavigateToWhatNew
            )

            // --- Legal & Privacy ---
            AboutPreferenceItem(
                title = "Open source licenses",
                onClick = {
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                }
            )

            AboutPreferenceItem(
                title = "Privacy policy",
                onClick = { uriHandler.openUri("https://seyone.dev/shot/privacy") }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Social Links ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SocialIconButton(
                    icon = Icons.Default.Public,
                    contentDescription = "Website",
                    onClick = { uriHandler.openUri("https://seyone.dev") }
                )
                SocialIconButton(
                    icon = Icons.Default.Code,
                    contentDescription = "GitHub",
                    onClick = { uriHandler.openUri("https://github.com/seyone22") }
                )
                SocialIconButton(
                    icon = Icons.Default.Email,
                    contentDescription = "Email",
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:hi@seyone.dev")
                            putExtra(Intent.EXTRA_SUBJECT, "Shot App Feedback")
                        }
                        context.startActivity(Intent.createChooser(intent, "Send Email"))
                    }
                )
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
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
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
fun SocialIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp) // Large touch target for Material Expressive
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}