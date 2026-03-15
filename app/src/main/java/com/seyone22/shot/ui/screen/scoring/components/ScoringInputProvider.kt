package com.seyone22.shot.ui.screen.scoring.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.seyone22.shot.data.local.entity.InputMethod
import com.seyone22.shot.ui.screens.scoring.ScoringUiState
import com.seyone22.shot.ui.screens.scoring.components.ScoringKeypad

@Composable
fun ScoringInputProvider(
    inputMethod: InputMethod,
    onValueInput: (String, Int, Boolean) -> Unit,
    onTargetInput: (Offset, Float) -> Unit,
    onBackspace: () -> Unit,
    onNextEnd: () -> Unit,
    state: ScoringUiState
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (inputMethod) {
                InputMethod.ARROW_VALUES -> {
                    ScoringKeypad(
                        onInput = onValueInput,
                        onBackspace = onBackspace,
                        onNextEnd = onNextEnd
                    )
                }
                InputMethod.TARGET_FACE -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ScoringTargetFace(
                            onTargetTap = onTargetInput,
                            modifier = Modifier.size(300.dp),
                            hits = state.currentEndHits, // <-- Feed the state list here!
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Control buttons for Target mode
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                onBackspace()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.AutoMirrored.Filled.Backspace, null)
                                Text(" Undo")
                            }
                            Button(onClick = {
                                onNextEnd()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }, modifier = Modifier.weight(1f)) {
                                Text("Next End")
                            }
                        }
                    }
                }
            }
        }
    }
}