package dev.seyone.shot.ui.screen.scoring

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.seyone.core.domain.repository.ArcherRepository
import dev.seyone.core.domain.repository.BowProfileRepository
import dev.seyone.core.domain.repository.LocationRepository
import dev.seyone.core.domain.repository.RoundRepository
import dev.seyone.core.domain.repository.ScoringRepository
import dev.seyone.core.domain.repository.SessionRepository
import dev.seyone.core.data.repository.ArrowSortOrder
import dev.seyone.core.data.repository.SettingsRepository
import dev.seyone.shot.ui.screen.scoring.components.OfficialScoresheetTable
import dev.seyone.shot.ui.screen.scoring.components.ScoringInputProvider
import dev.seyone.shot.ui.theme.ArcheryColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    sessionId: Long,
    scoringRepository: ScoringRepository,
    sessionRepository: SessionRepository,
    roundRepository: RoundRepository,
    locationRepository: LocationRepository,
    bowProfileRepository: BowProfileRepository,
    archerRepository: ArcherRepository,
    settingsRepository: SettingsRepository,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ScoringViewModel = viewModel(
        factory = ScoringViewModel.Factory(
            sessionId = sessionId,
            scoringRepository = scoringRepository,
            sessionRepository = sessionRepository,
            roundRepository = roundRepository,
            locationRepository = locationRepository,
            bowProfileRepository = bowProfileRepository,
            archerRepository = archerRepository
        )
    )

    val state by viewModel.uiState.collectAsState()
    val userSettings by settingsRepository.settings.collectAsState()
    val listState = rememberLazyListState()

    // 1. Get the Android context for the Toast
    val context = LocalContext.current

    // 2. Add the Event Listener
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is ScoringUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 2. Trigger an auto-scroll whenever the active end changes, or an arrow is added to the current end
    LaunchedEffect(state.currentEndIndex, state.ends.getOrNull(state.currentEndIndex)?.size) {
        // Calculate the exact list index based on headers and ends
        var targetListIndex = 0
        var endsCounted = 0

        for (dist in state.distances) {
            targetListIndex++ // Add 1 for the Distance Header item itself

            if (state.currentEndIndex < endsCounted + dist.numberOfEnds) {
                // The current end is inside this distance block. Add the offset and stop.
                targetListIndex += (state.currentEndIndex - endsCounted)
                break
            }
            // Otherwise, add all ends from this distance and keep searching
            targetListIndex += dist.numberOfEnds
            endsCounted += dist.numberOfEnds
        }

        // Fallback if distances are somehow empty
        val finalIndex =
            if (state.distances.isEmpty()) state.currentEndIndex + 1 else targetListIndex

        // Use animateScrollToItem for a smooth, user-friendly transition
        listState.animateScrollToItem(index = finalIndex)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(state.title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (state.viewMode == ScoringViewMode.INPUT) {
                                Icons.AutoMirrored.Filled.Assignment
                            } else {
                                Icons.Default.Edit
                            },
                            contentDescription = if (state.viewMode == ScoringViewMode.INPUT) {
                                "View Official Scoresheet"
                            } else {
                                "Return to Scoring Entry"
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (state.viewMode == ScoringViewMode.INPUT) {
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
                                Text(
                                    "Average",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = state.totalScore.toString(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // The Keyboard / Target Input
                    ScoringInputProvider(
                        inputMethod = state.inputMethod,
                        onValueInput = viewModel::onArrowInput,
                        onTargetInput = viewModel::onTargetInput,
                        onBackspace = viewModel::onBackspace,
                        onNextEnd = viewModel::onNextEnd,
                        state = state
                    )
                }
            }
        }
    ) { innerPadding ->
        if (state.viewMode == ScoringViewMode.SCORESHEET) {
            OfficialScoresheetTable(
                state = state,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
            // Keep track of our global index across multiple distances
            var currentEndGlobalIndex = 0

            state.distances.forEach { distance ->
                // 1. Distance Header
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Distance: ${distance.distanceValue}${distance.distanceUnit.name.take(1).lowercase()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${distance.numberOfEnds} Ends",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 2. Slice the global list of ends to only show the ones for this distance
                val endsInThisDistance = state.ends.drop(currentEndGlobalIndex).take(distance.numberOfEnds)

                // --- THE FIX: Capture the index right here! ---
                val capturedStartIndex = currentEndGlobalIndex

                // 3. Render the End Rows
                itemsIndexed(endsInThisDistance) { localIndex, arrows ->
                    // Use the frozen captured index instead of the mutating 'var'
                    val globalIndex = capturedStartIndex + localIndex

                    EndRow(
                        endNumber = globalIndex + 1,
                        arrows = arrows,
                        arrowsPerEnd = distance.arrowsPerEnd,
                        selectedArrowIndex = if (state.selectedEndIndex == globalIndex) state.selectedArrowIndex else null,
                        arrowSortOrder = userSettings.arrowSortOrder,
                        onArrowClick = { arrowIdx ->
                            viewModel.selectArrowForEdit(globalIndex, arrowIdx)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }

                // Increment our global tracker for the next distance in the loop
                currentEndGlobalIndex += distance.numberOfEnds
            }

            // Add a spacer at the bottom so the last row isn't hidden behind the keypad
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
}

@Composable
fun EndRow(
    endNumber: Int,
    arrows: List<ArrowInput>,
    arrowsPerEnd: Int, // Pass this to know how many slots to draw
    selectedArrowIndex: Int?,
    arrowSortOrder: ArrowSortOrder = ArrowSortOrder.AS_ENTERED,
    onArrowClick: (Int) -> Unit
) {
    val displaySlots = remember(arrows, arrowsPerEnd, arrowSortOrder) {
        val sortedPairs = sortIndexedArrows(arrows, arrowSortOrder)
        val list = mutableListOf<Pair<Int, ArrowInput?>>()
        for (i in 0 until arrowsPerEnd) {
            if (i < sortedPairs.size) {
                list.add(sortedPairs[i])
            } else {
                list.add(i to null)
            }
        }
        list
    }

    Surface(
        // Subtle background highlight if this entire row is being edited
        color = if (selectedArrowIndex != null) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$endNumber.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (slot in displaySlots) {
                    val (originalIndex, arrow) = slot
                    val isSelected = selectedArrowIndex == originalIndex

                    ArrowCircle(
                        arrow = arrow,
                        isSelected = isSelected,
                        onClick = { onArrowClick(originalIndex) }
                    )
                }
            }

            val endTotal = arrows.sumOf { it.score }
            Text(
                text = "$endTotal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun sortIndexedArrows(
    arrows: List<ArrowInput>,
    sortOrder: ArrowSortOrder
): List<Pair<Int, ArrowInput>> {
    val indexedPairs = arrows.mapIndexed { idx, arrow -> idx to arrow }
    if (sortOrder == ArrowSortOrder.AS_ENTERED) {
        return indexedPairs
    }
    return indexedPairs.sortedWith(Comparator { a, b ->
        val weightA = getArrowValueWeight(a.second.value)
        val weightB = getArrowValueWeight(b.second.value)
        weightB.compareTo(weightA)
    })
}

private fun getArrowValueWeight(value: String): Int {
    return when (value.uppercase()) {
        "X" -> 11
        "10" -> 10
        "9" -> 9
        "8" -> 8
        "7" -> 7
        "6" -> 6
        "5" -> 5
        "4" -> 4
        "3" -> 3
        "2" -> 2
        "1" -> 1
        "M", "0" -> 0
        else -> value.toIntOrNull() ?: -1
    }
}

@Composable
fun ArrowCircle(
    arrow: ArrowInput?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // If arrow is null, it's an empty slot
    val bgColor =
        if (arrow == null) MaterialTheme.colorScheme.surfaceVariant else when (arrow.value) {
            "X", "10", "9" -> ArcheryColors.Gold
            "8", "7" -> ArcheryColors.Red
            "6", "5" -> ArcheryColors.Blue
            "4", "3" -> ArcheryColors.Black
            "2", "1" -> ArcheryColors.White
            "M" -> ArcheryColors.Miss
            else -> Color.Gray
        }

    val textColor = if (arrow == null) Color.Transparent else when (arrow.value) {
        "X", "10", "9" -> ArcheryColors.GoldText
        "8", "7" -> ArcheryColors.RedText
        "6", "5" -> ArcheryColors.BlueText
        "4", "3" -> ArcheryColors.BlackText
        "2", "1" -> ArcheryColors.WhiteText
        "M" -> ArcheryColors.MissText
        else -> Color.White
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        // Draw a thick primary border if selected, otherwise standard border for white arrows
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        else if (bgColor == ArcheryColors.White) BorderStroke(1.dp, Color.LightGray)
        else null,
        modifier = Modifier.size(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (arrow != null) {
                Text(
                    text = arrow.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}