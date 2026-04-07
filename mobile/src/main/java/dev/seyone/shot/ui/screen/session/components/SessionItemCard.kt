package dev.seyone.shot.ui.screen.session.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.seyone.shot.data.local.entity.SessionEntity
import dev.seyone.shot.data.local.entity.SessionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionItemCard(
    session: SessionEntity,
    roundName: String, // Pass the resolved name here
    totalScore: Int,   // <-- ADDED
    average: Float,    // <-- ADDED
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Slightly more rounded for M3 Expressive
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // --- TOP ROW: Badge & Date ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Session Type Pill Badge
                SessionTypeBadge(type = session.sessionType)

                // Date with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = "Date",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(session.timestamp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- MAIN TITLE: Round Name ---
            Text(
                text = roundName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM SECTION: Details & Score ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Side: Minor details (Archers, Input Method)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetailRow(
                        icon = Icons.Outlined.People,
                        text = "${session.numberOfArchers} Archer(s)"
                    )

                    val inputIcon = if (session.inputMethod.name.contains("FACE")) {
                        Icons.Outlined.TrackChanges
                    } else {
                        Icons.Outlined.Edit
                    }
                    DetailRow(
                        icon = inputIcon,
                        text = session.inputMethod.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                    )
                }

                // Right Side: Scores (Placeholders until the scoring engine is built)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = totalScore.toString(), // <-- UPDATED
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Avg: ${String.format(Locale.getDefault(), "%.1f", average)}", // <-- UPDATED
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionTypeBadge(type: SessionType) {
    val backgroundColor = if (type == SessionType.COMPETITION) {
        MaterialTheme.colorScheme.errorContainer // Makes competition stand out
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (type == SessionType.COMPETITION) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = contentColor
        )
    }
}

@Composable
private fun DetailRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}