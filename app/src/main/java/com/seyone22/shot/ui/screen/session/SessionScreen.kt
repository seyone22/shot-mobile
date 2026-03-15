package com.seyone22.shot.ui.screen.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyone22.shot.data.domain.repository.RoundRepository
import com.seyone22.shot.data.domain.repository.ScoringRepository
import com.seyone22.shot.data.domain.repository.SessionRepository
import com.seyone22.shot.data.local.entity.SessionEntity
import com.seyone22.shot.ui.screen.session.components.NewSessionDialog
import com.seyone22.shot.ui.screen.session.components.SessionItemCard
import com.seyone22.shot.ui.screen.session.components.SessionNotesDialog
import com.seyone22.shot.ui.screen.session.components.SessionSummaryBottomSheet
import com.seyone22.shot.ui.screens.session.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    sessionRepository: SessionRepository,
    roundRepository: RoundRepository,
    scoringRepository: ScoringRepository, // <-- ADDED
    onNavigateToScoring: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // Initialize ViewModel using our manual DI factory, now with both repositories
    val viewModel: SessionViewModel = viewModel(
        factory = SessionViewModel.Factory(sessionRepository, roundRepository, scoringRepository)
    )
    val sessions by viewModel.sessionList.collectAsState()
    val availableRounds by viewModel.availableRounds.collectAsState() // Observe real rounds!
    val summaryData by viewModel.sessionSummary.collectAsState() // <-- OBSERVE NEW STATE

    // Scroll behavior for the collapsing TopAppBar
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // State to handle the Bottom Sheet visibility
    var showNewSessionSheet by rememberSaveable { mutableStateOf(false) }

    // State to handle the "Summary" Bottom Sheet visibility
    var selectedSessionForSummary by remember { mutableStateOf<SessionEntity?>(null) }

    var showNotesDialogFor by remember { mutableStateOf<SessionEntity?>(null) } // <--- ADD THIS

    var sessionToDelete by remember { mutableStateOf<SessionEntity?>(null) }

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        TopAppBar(
            title = { Text("Sessions") }, actions = {
                IconButton(onClick = { /* TODO: Implement Search */ }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { /* TODO: Implement Filter */ }) {
                    Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                }
            }, scrollBehavior = scrollBehavior
        )
    }, floatingActionButton = {
        ExtendedFloatingActionButton(
            text = { Text("New Session") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Start Session") },
            onClick = { showNewSessionSheet = true },
            expanded = scrollBehavior.state.collapsedFraction < 0.5f // Shrinks to a normal FAB when scrolling down
        )
    }) { innerPadding ->

        // --- 1. Main Content ---
        if (sessions.isEmpty()) {
            EmptySessionState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp, // Extra padding for FAB
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sessions, key = { it.id }) { session ->
                    val roundName =
                        availableRounds.find { it.round.id == session.roundId }?.round?.name
                            ?: "Unknown Round (ID: ${session.roundId})"

                    SessionItemCard(
                        session = session, roundName = roundName, onClick = {
                            selectedSessionForSummary = session
                            viewModel.loadSessionSummary(session.id) // <-- TRIGGER FETCH
                        })
                }
            }
        } // <--- Notice the closing brace for the 'else' block is here now!

        // --- 2. Overlays & Bottom Sheets (Outside the if/else) ---

        // Show the Modal Bottom Sheet when FAB is clicked
        if (showNewSessionSheet) {
            NewSessionDialog(
                rounds = availableRounds,
                onDismiss = { showNewSessionSheet = false },
                onStartSession = { roundId, type, method, archers, arrows ->
                    showNewSessionSheet = false
                    viewModel.startNewSession(
                        roundId, type, method, archers, arrows
                    ) { newSessionId ->
                        onNavigateToScoring(newSessionId)
                    }
                })
        }

        selectedSessionForSummary?.let { sessionToSummarize ->
            val roundName =
                availableRounds.find { it.round.id == sessionToSummarize.roundId }?.round?.name
                    ?: "Unknown Round"

            SessionSummaryBottomSheet(
                session = sessionToSummarize,
                roundName = roundName,
                summaryData = summaryData, // <-- PASS REAL DATA HERE
                onDismiss = {
                    selectedSessionForSummary = null
                    viewModel.clearSessionSummary() // Cleanup
                },
                onEditClick = {
                    selectedSessionForSummary = null // Close sheet
                    onNavigateToScoring(sessionToSummarize.id) // Navigate to the scoring screen
                },
                onDeleteClick = {
                    // Instead of deleting immediately, trigger the confirmation dialog
                    sessionToDelete = sessionToSummarize
                    selectedSessionForSummary = null
                },
                onNotesClick = {
                    showNotesDialogFor = sessionToSummarize // Trigger the dialog
                    selectedSessionForSummary =
                        null // Optionally close the bottom sheet to focus on typing
                })
        }
    }

    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Session?") },
            text = { Text("This will permanently remove this session and all its recorded scores. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession(session)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            })
    }

    // 3. Notes Dialog
    showNotesDialogFor?.let { session ->
        SessionNotesDialog(
            initialNotes = session.notes,
            onDismiss = { showNotesDialogFor = null },
            onSave = { updatedNotes ->
                viewModel.updateSessionNotes(session, updatedNotes)
                showNotesDialogFor = null
            })
    }
}


@Composable
fun EmptySessionState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No sessions yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
