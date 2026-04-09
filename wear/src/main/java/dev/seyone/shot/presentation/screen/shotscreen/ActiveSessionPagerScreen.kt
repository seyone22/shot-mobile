package dev.seyone.shot.presentation.screen.shotscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.pager.HorizontalPager
import androidx.wear.compose.foundation.pager.rememberPagerState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.HorizontalPageIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.rememberPickerState
import dev.seyone.core.domain.InputMethod
import dev.seyone.shot.presentation.screen.analysis.AnalysisPage

@Composable
fun ActiveSessionPagerScreen(viewModel: ShotScreenViewModel, onNavigateBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val session = state.activeSession ?: return

    ScreenScaffold {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState, modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        if (session.inputMethod == InputMethod.ARROW_VALUES) { // Make sure this matches your Domain Enum
                            NumericPickerScorer(
                                currentArrowsCount = state.currentEndArrows.size,
                                arrowsPerEnd = session.arrowsPerEnd,
                                onArrowAdded = { score, isX -> viewModel.addArrow(score, isX) },
                                onEndComplete = { viewModel.completeEnd() },
                                onUndo = { viewModel.undoLastAction() })
                        } else {
                            TargetFaceScorer(
                                currentArrowsCount = state.currentEndArrows.size,
                                arrowsPerEnd = session.arrowsPerEnd,
                                onArrowAdded = { score, x, y ->
                                    viewModel.addArrow(
                                        score, false, x, y
                                    )
                                },
                                onEndComplete = { viewModel.completeEnd() },
                                onUndo = { viewModel.undoLastAction() })
                        }
                    }

                    1 -> AnalysisPage(state)
                }
            }

            // Moved up to prevent overlapping with the EdgeButton
            HorizontalPageIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            )
        }
    }
}

@Composable
fun NumericPickerScorer(
    currentArrowsCount: Int,
    arrowsPerEnd: Int,
    onArrowAdded: (Int, Boolean) -> Unit,
    onEndComplete: () -> Unit,
    onUndo: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val pickerOptions = listOf("M", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "X")

    // Logic fix: Prevents "Arrow 4 of 3"
    val isEndComplete = currentArrowsCount >= arrowsPerEnd

    val pickerState = rememberPickerState(
        initialNumberOfOptions = pickerOptions.size, initiallySelectedIndex = 10
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = 28.dp, bottom = 52.dp
                ), // Safe-zone padding for TimeText & EdgeButton
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header Row: Keeps the Undo button neatly inline instead of floating awkwardly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentArrowsCount > 0 || isEndComplete) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onUndo()
                        },
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = if (isEndComplete) "End Complete" else "Arrow ${currentArrowsCount + 1} of $arrowsPerEnd",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isEndComplete) {
                // Visual feedback that the end is done, removing the picker entirely
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Complete",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .size(64.dp)
                        .weight(1f)
                )
            } else {
                // High-Performance Picker
                Picker(
                    state = pickerState,
                    contentDescription = { "Score Picker" },
                    modifier = Modifier.weight(1f)
                ) { optionIndex ->

                    // Derived state prevents lag by caching the selection calculation
                    val isSelected by remember { derivedStateOf { pickerState.selectedOptionIndex == optionIndex } }

                    Text(
                        text = pickerOptions[optionIndex],
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        // graphicsLayer updates drawing properties without triggering recomposition
                        modifier = Modifier.graphicsLayer {
                            alpha = if (isSelected) 1f else 0.4f
                            scaleX = if (isSelected) 1f else 0.8f
                            scaleY = if (isSelected) 1f else 0.8f
                        })
                }
            }
        }

        // Context-Aware Action Button
        EdgeButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                if (isEndComplete) {
                    onEndComplete()
                } else {
                    when (val selected = pickerOptions[pickerState.selectedOptionIndex]) {
                        "M" -> onArrowAdded(0, false)
                        "X" -> onArrowAdded(10, true)
                        else -> onArrowAdded(selected.toInt(), false)
                    }
                }
            }, modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(if (isEndComplete) "Finish End" else "Add Arrow")
        }
    }
}


@Composable

fun TargetFaceScorer(
    currentArrowsCount: Int,
    arrowsPerEnd: Int,
    onArrowAdded: (Int, Float, Float) -> Unit,
    onEndComplete: () -> Unit,
    onUndo: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎯", style = MaterialTheme.typography.displayLarge)
            Text(
                "Arrow ${currentArrowsCount + 1}/$arrowsPerEnd",
                style = MaterialTheme.typography.labelSmall
            )

// Mocking a tap on the target face returning a 9 at coordinates
            Button(onClick = { onArrowAdded(9, 12.5f, -4.0f) }) {
                Text("Simulate Plotted Shot")
            }
        }

        Button(
            onClick = onUndo,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(36.dp)
        ) {
            Text("↺")
        }

        EdgeButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEndComplete()
            },

            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("Finish End")
        }
    }
}