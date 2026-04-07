package dev.seyone.shot.ui.screen.scoring.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.seyone.shot.ui.theme.ArcheryColors

@Composable
fun ScoringTargetFace(
    onTargetTap: (Offset, Float) -> Unit,
    hits: List<Offset>, // <-- NEW PARAMETER: List of points to draw
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // We use M3's outlineVariant color for the markers to ensure contrast across themes
    val markerColor = MaterialTheme.colorScheme.outlineVariant

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val center = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
        val targetRadius = constraints.maxWidth / 2f
        val ringWidth = targetRadius / 10f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onTargetTap(offset, targetRadius)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
        ) {
            // --- Existing Ring Drawing Logic (from your provided code) ---
            val colors = listOf(ArcheryColors.White, ArcheryColors.Black, ArcheryColors.Blue, ArcheryColors.Red, ArcheryColors.Gold)
            for (i in 0 until 5) {
                val radius = targetRadius - (i * 2 * ringWidth)
                drawCircle(color = colors[i], radius = radius, center = center)
            }
            for (i in 1..10) {
                drawCircle(color = Color.Gray.copy(alpha = 0.3f), radius = i * ringWidth, center = center, style = Stroke(width = 1.dp.toPx()))
            }
            drawCircle(color = Color.Black.copy(alpha = 0.2f), radius = ringWidth / 2f, center = center, style = Stroke(width = 1.dp.toPx()))
            // --- End of Existing Logic ---

            // --- THE MAGIC ADD-ON: Draw Shot Markers ---
            // Now that the rings are drawn, layer the 'hits' on top!
            hits.forEach { hitCoordinate ->
                // Draw a simple, solid circle at the tap position
                drawCircle(
                    color = markerColor,
                    radius = 5.dp.toPx(), // Set the size of the marker
                    center = hitCoordinate // Use the exact captured offset
                )
            }
        }
    }
}