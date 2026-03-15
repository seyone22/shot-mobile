package com.seyone22.shot.ui.screen.scoring

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyone22.shot.data.domain.repository.RoundRepository
import com.seyone22.shot.data.domain.repository.ScoringRepository
import com.seyone22.shot.data.domain.repository.SessionRepository
import com.seyone22.shot.ui.screen.scoring.components.ScoringInputProvider
import com.seyone22.shot.ui.screens.scoring.ArrowInput
import com.seyone22.shot.ui.screens.scoring.ScoringViewModel
import com.seyone22.shot.ui.screens.scoring.components.ScoringKeypad
import com.seyone22.shot.ui.theme.ArcheryColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    sessionId: Long,
    scoringRepository: ScoringRepository,
    sessionRepository: SessionRepository,
    roundRepository: RoundRepository, // <-- 1. Add this parameter
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ScoringViewModel = viewModel(
        factory = ScoringViewModel.Factory(
            sessionId = sessionId,
            scoringRepository = scoringRepository,
            sessionRepository = sessionRepository,
            roundRepository = roundRepository // <-- 2. Pass it to the factory
        )
    )

    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // 2. Trigger an auto-scroll whenever the active end changes, or an arrow is added to the current end
    LaunchedEffect(state.currentEndIndex, state.ends.getOrNull(state.currentEndIndex)?.size) {
        // Index 0 is the "Distance" header item.
        // Therefore, the current end's list index is currentEndIndex + 1.
        val targetIndex = state.currentEndIndex + 1

        // Use animateScrollToItem for a smooth, user-friendly transition
        listState.animateScrollToItem(index = targetIndex)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.title, fontWeight = FontWeight.Bold) },
                navigationIcon = { // <-- Add the M3 Navigation Icon
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Notes */ }) {
                        Icon(Icons.Default.Assignment, contentDescription = "Notes")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Persistent Summary Bar above the keyboard
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Average", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f", state.average),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Small pill for Golds/10s
                        Surface(
                            shape = CircleShape,
                            color = ArcheryColors.Gold,
                            contentColor = ArcheryColors.GoldText
                        ) {
                            Text(
                                text = "Golds: ${state.golds}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = state.totalScore.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // The Keyboard
                ScoringInputProvider(
                    inputMethod = state.inputMethod, // This comes from your SessionEntity
                    onValueInput = viewModel::onArrowInput,
                    onTargetInput = viewModel::onTargetInput,
                    onBackspace = viewModel::onBackspace,
                    onNextEnd = viewModel::onNextEnd,
                    state = state
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState, // 3. Attach the list state to the LazyColumn
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Header for the specific distance being shot (Item Index 0)
            item {
                Text(
                    text = "Distance: ${state.distance}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // The list of ends (Item Indexes 1 to N)
            itemsIndexed(state.ends) { index, arrows ->
                EndRow(endNumber = index + 1, arrows = arrows)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun EndRow(endNumber: Int, arrows: List<ArrowInput>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // End Number Indicator
        Text(
            text = "$endNumber.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )

        // Arrow Slots (Handles standard 3 or 6 arrow ends beautifully)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 0 until 6) {
                if (i < arrows.size) {
                    ArrowCircle(arrow = arrows[i])
                } else if (i < 3 || arrows.size > 3) {
                    // Render empty slots to guide the user visually.
                    // Only show 4-6 if they've passed 3 arrows.
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }

        // End Total Score
        val endTotal = arrows.sumOf { it.score }
        Text(
            text = endTotal.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(48.dp)
        )
    }
}

@Composable
fun ArrowCircle(arrow: ArrowInput) {
    val bgColor = when (arrow.value) {
        "X", "10", "9" -> ArcheryColors.Gold
        "8", "7" -> ArcheryColors.Red
        "6", "5" -> ArcheryColors.Blue
        "4", "3" -> ArcheryColors.Black
        "2", "1" -> ArcheryColors.White
        "M" -> ArcheryColors.Miss
        else -> Color.Gray
    }
    val textColor = when (arrow.value) {
        "X", "10", "9" -> ArcheryColors.GoldText
        "8", "7" -> ArcheryColors.RedText
        "6", "5" -> ArcheryColors.BlueText
        "4", "3" -> ArcheryColors.BlackText
        "2", "1" -> ArcheryColors.WhiteText
        "M" -> ArcheryColors.MissText
        else -> Color.White
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor)
            // Add a subtle border for the white text/background so it doesn't bleed into the app background
            .then(if (bgColor == ArcheryColors.White) Modifier.background(Color.LightGray.copy(alpha=0.5f)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = arrow.value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}