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
import dev.seyone.core.domain.TargetFaceSize
import dev.seyone.shot.ui.theme.ArcheryColors

@Composable
fun ScoringTargetFace(
    onTargetTap: (Offset, Float, TargetFaceSize) -> Unit, // Updated signature
    hits: List<Offset>,
    modifier: Modifier = Modifier,
    targetFace: TargetFaceSize = TargetFaceSize.CM_122 // Feed directly from DB state
) {
    val haptic = LocalHapticFeedback.current
    val markerColor = MaterialTheme.colorScheme.outlineVariant
    val markerRadius = 5.dp

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val center = Offset(constraints.maxWidth / 2f, constraints.maxHeight / 2f)
        val targetRadius = constraints.maxWidth / 2f

        // Divide the radius by the amount of physical rings on this specific paper
        val ringWidth = targetRadius / targetFace.rings

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(targetFace) {
                    detectTapGestures { offset ->
                        onTargetTap(offset, targetRadius, targetFace)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
        ) {
            // 1. Draw Rings (From outside in)
            // If rings = 5 (Triple Spot), starting score is 11 - 5 = 6.
            // It will only draw the rings for 6, 7, 8, 9, 10.
            val startingScore = 11 - targetFace.rings

            for (score in startingScore..10) {
                val currentRingIndex = 11 - score
                val currentRadius = currentRingIndex * ringWidth

                drawCircle(
                    color = getWaRingColor(score),
                    radius = currentRadius,
                    center = center
                )
            }

            // 2. Draw Ring Separator Lines
            for (i in 1..targetFace.rings) {
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.5f),
                    radius = i * ringWidth,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 3. Draw Inner 10 (X-Ring)
            drawCircle(
                color = Color.Black.copy(alpha = 0.3f),
                radius = ringWidth / 2f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // 4. Draw Shot Markers
            hits.forEach { hitCoordinate ->
                drawCircle(
                    color = markerColor,
                    radius = markerRadius.toPx(),
                    center = hitCoordinate
                )
            }
        }
    }
}

/**
 * Helper to determine standard World Archery colors based on ring value.
 */
private fun getWaRingColor(score: Int): Color {
    return when (score) {
        10, 9 -> ArcheryColors.Gold
        8, 7 -> ArcheryColors.Red
        6, 5 -> ArcheryColors.Blue
        4, 3 -> ArcheryColors.Black
        2, 1 -> ArcheryColors.White
        else -> Color.Transparent
    }
}

/**
 * Calculates the score natively handling Imperial, Standard Metric, and Compound Inner-10 math.
 */
fun calculateArrowScore(
    tapOffset: Offset,
    center: Offset,
    targetRadius: Float,
    targetFace: TargetFaceSize,
    scoringMethod: String // Passed from your DB (e.g. "METRIC_INNER_10")
): String {
    val distance = (tapOffset - center).getDistance()

    // Automatic Miss if tapped outside the paper bounds
    if (distance > targetRadius) return "M"

    val ringWidth = targetRadius / targetFace.rings
    val scoreOffset = (distance / ringWidth).toInt() // 0 = Innermost ring

    // Check for dead-center X-Ring
    val isX = distance <= (ringWidth / 2f)

    return when (scoringMethod) {
        "IMPERIAL_5_ZONE" -> {
            // Imperial is shot on a 10-ring face, but scored in 5 color zones (9, 7, 5, 3, 1)
            // This integer math naturally pairs the 10 rings into 5 zones.
            var score = 9 - ((scoreOffset / 2) * 2)
            if (score > 9) score = 9
            score.toString()
        }
        "METRIC_INNER_10" -> {
            // Compound Indoor Rule: Only the tiny X-ring is a 10. The rest of the gold is a 9.
            if (isX) "X" // Or "10" depending on your ViewModel preference
            else if (scoreOffset == 0) "9"
            else (10 - scoreOffset).toString()
        }
        else -> {
            // "METRIC_10_ZONE" (Standard Recurve / Vegas Math)
            if (isX) "X" else {
                var score = 10 - scoreOffset
                if (score > 10) score = 10
                score.toString()
            }
        }
    }
}