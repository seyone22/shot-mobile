package com.seyone22.shot.ui.screen.session.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seyone22.shot.data.local.entity.SessionEntity
import com.seyone22.shot.ui.screens.session.SessionSummaryData
import com.seyone22.shot.ui.theme.ArcheryColors
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
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
        if (scores.size < 2) return@Canvas // Need at least 2 points to draw a line

        val maxScore = scores.maxOrNull()?.toFloat() ?: 1f
        val minScore = scores.minOrNull()?.toFloat() ?: 0f

        // Add some padding to Y axis so the line doesn't hit the absolute top/bottom
        val yRange = (maxScore - minScore).coerceAtLeast(1f)
        val paddingY = yRange * 0.2f
        val actualMax = maxScore + paddingY
        val actualMin = (minScore - paddingY).coerceAtLeast(0f)
        val actualRange = actualMax - actualMin

        val widthPerStep = size.width / (scores.size - 1)

        val path = Path()
        val points = mutableListOf<Offset>()

        scores.dropLastWhile { it == 0 }.forEachIndexed { index, score ->
            val x = index * widthPerStep
            // Calculate Y position (invert because Y=0 is the top of the canvas)
            val y = size.height - ((score - actualMin) / actualRange * size.height)
            val offset = Offset(x, y)
            points.add(offset)

            if (index == 0) path.moveTo(offset.x, offset.y)
            else path.lineTo(offset.x, offset.y)
        }

        // Draw the connecting line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 4.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw the dots on top
        points.forEach { point ->
            drawCircle(
                color = dotColor,
                radius = 5.dp.toPx(),
                center = point
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}