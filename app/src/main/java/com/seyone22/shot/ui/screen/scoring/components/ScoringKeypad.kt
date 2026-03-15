package com.seyone22.shot.ui.screens.scoring.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.seyone22.shot.ui.theme.ArcheryColors

@Composable
fun ScoringKeypad(
    onInput: (String, Int, Boolean) -> Unit,
    onBackspace: () -> Unit,
    onNextEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp, // Makes it pop like a system keyboard
        shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(260.dp), // <--- THE FIX: Explicit height constraint
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Col 1: X, 7, 3
            KeypadColumn(modifier = Modifier.weight(1f)) {
                ScoreButton("X", 10, ArcheryColors.Gold, ArcheryColors.GoldText, true, onInput)
                ScoreButton("7", 7, ArcheryColors.Red, ArcheryColors.RedText, false, onInput)
                ScoreButton("3", 3, ArcheryColors.Black, ArcheryColors.BlackText, false, onInput)
            }
            // Col 2: 10, 6, 2
            KeypadColumn(modifier = Modifier.weight(1f)) {
                ScoreButton("10", 10, ArcheryColors.Gold, ArcheryColors.GoldText, false, onInput)
                ScoreButton("6", 6, ArcheryColors.Blue, ArcheryColors.BlueText, false, onInput)
                ScoreButton("2", 2, ArcheryColors.White, ArcheryColors.WhiteText, false, onInput)
            }
            // Col 3: 9, 5, 1
            KeypadColumn(modifier = Modifier.weight(1f)) {
                ScoreButton("9", 9, ArcheryColors.Gold, ArcheryColors.GoldText, false, onInput)
                ScoreButton("5", 5, ArcheryColors.Blue, ArcheryColors.BlueText, false, onInput)
                ScoreButton("1", 1, ArcheryColors.White, ArcheryColors.WhiteText, false, onInput)
            }
            // Col 4: 8, 4, M
            KeypadColumn(modifier = Modifier.weight(1f)) {
                ScoreButton("8", 8, ArcheryColors.Red, ArcheryColors.RedText, false, onInput)
                ScoreButton("4", 4, ArcheryColors.Black, ArcheryColors.BlackText, false, onInput)
                ScoreButton("M", 0, ArcheryColors.Miss, ArcheryColors.MissText, false, onInput)
            }
            // Col 5: Backspace & Next End
            Column(
                modifier = Modifier.weight(1.2f), // Slightly wider for actions
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onBackspace,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = onNextEnd,
                    modifier = Modifier.fillMaxWidth().weight(2f), // Spans two rows
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Next\nEnd", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun KeypadColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
private fun ColumnScope.ScoreButton(
    label: String,
    score: Int,
    bgColor: Color,
    textColor: Color,
    isX: Boolean,
    onInput: (String, Int, Boolean) -> Unit
) {
    Button(
        onClick = { onInput(label, score, isX) },
        modifier = Modifier.fillMaxWidth().weight(1f),
        colors = ButtonDefaults.buttonColors(containerColor = bgColor, contentColor = textColor),
        shape = RoundedCornerShape(12.dp),
        border = if (bgColor == ArcheryColors.White) BorderStroke(1.dp, Color.LightGray) else null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}