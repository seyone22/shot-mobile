package dev.seyone.shot.ui.screen.session.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import dev.seyone.shot.data.local.entity.SessionEntity
import dev.seyone.shot.ui.screens.session.SessionSummaryData
import dev.seyone.shot.ui.theme.ArcheryColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryBottomSheet(
    session: SessionEntity,
    roundName: String,
    summaryData: SessionSummaryData?, // Passed in from ViewModel
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNotesClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- Action Row (Google Photos Style) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ActionIcon(Icons.Outlined.Share, "Share", onClick = { /* TODO */ })
                ActionIcon(Icons.Outlined.Edit, "Resume", onClick = onEditClick)
                ActionIcon(Icons.Outlined.Notes, "Notes", onClick = onNotesClick)
                ActionIcon(Icons.Outlined.Delete, "Trash", onClick = onDeleteClick)
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // --- Header ---
            Column {
                Text(
                    text = roundName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatDate(session.timestamp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (session.notes.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (summaryData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                // --- Score Trend Chart ---
                if (summaryData.endScores.isNotEmpty()) {
                    Text("End Progression", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    EndScoreLineChart(
                        scores = summaryData.endScores,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }

                // --- Metrics Card ---
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem("Total", summaryData.totalScore.toString())
                            MetricItem("Average", String.format(Locale.getDefault(), "%.2f", summaryData.average))
                            MetricItem("Golds", summaryData.golds.toString(), ArcheryColors.GoldText)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem("Hits", "${summaryData.hits} / ${summaryData.totalArrowsShot}")
                            MetricItem("X's", summaryData.xs.toString())
                        }
                    }
                }
            }
        }
    }
}

// --- Google Photos Style Action Item ---
@Composable
private fun ActionIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricItem(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

// --- Native Canvas Line Chart ---

@Composable
fun EndScoreLineChart(scores: List<Int>, modifier: Modifier = Modifier) {
    if (scores.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }

    // LaunchedEffect reacts to changes in the 'scores' list
    LaunchedEffect(scores) {
        modelProducer.runTransaction {
            lineSeries {
                // Pass your dynamic list directly into the series
                series(scores)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}