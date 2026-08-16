package dev.seyone.shot.ui.screen.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
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
import dev.seyone.core.data.entity.SessionEntity
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.model.Session
import dev.seyone.shot.di.AppViewModelProvider
import dev.seyone.shot.ui.screen.session.components.NewSessionDialog
import dev.seyone.shot.ui.screen.session.components.SessionItemCard
import dev.seyone.shot.ui.screen.session.components.SessionNotesDialog
import dev.seyone.shot.ui.screen.session.components.SessionSummaryBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    modifier: Modifier = Modifier,
    viewModel: SessionViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onNavigateToScoring: (Long) -> Unit,
) {
    val sessions by viewModel.filteredSessionList.collectAsState()
    val availableRounds by viewModel.availableRounds.collectAsState()
    val summaryData by viewModel.sessionSummary.collectAsState()
    val savedLocations by viewModel.locations.collectAsState()
    val savedBowProfiles by viewModel.bowProfiles.collectAsState()
    val savedArchers by viewModel.archers.collectAsState()
    val savedArrowSets by viewModel.arrowSets.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()

    // Search State
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Filter State
    var showFilterMenu by remember { mutableStateOf(false) }
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    // Scroll behavior for the collapsing TopAppBar
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Bottom Sheet & Dialog States
    var showNewSessionSheet by rememberSaveable { mutableStateOf(false) }
    var selectedSessionForSummary by remember { mutableStateOf<Session?>(null) }
    var editingSession by remember { mutableStateOf<Session?>(null) }
    var showNotesDialogFor by remember { mutableStateOf<Session?>(null) }
    var sessionToDelete by remember { mutableStateOf<Session?>(null) }
    val truncationWarning by viewModel.truncationWarning.collectAsState()

    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        if (isSearchActive) {
            // --- MATERIAL 3 SEARCH BAR ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                @Suppress("DEPRECATION") SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onSearch = { /* Execute search / Hide keyboard if needed */ },
                    active = false, // Kept false so it acts as a floating bar instead of taking over the screen
                    onActiveChange = { },
                    placeholder = { Text("Search rounds or notes...") },
                    leadingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            viewModel.updateSearchQuery("") // Clear search on exit
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Search"
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Content for expanded state (unused since active = false)
                }
            }
        } else {
            // --- DEFAULT TOP BAR ---
            TopAppBar(
                title = { Text("Sessions") }, actions = {
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    }

                    // Filter Dropdown Wrapper
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (selectedFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }) {
                            DropdownMenuItem(text = { Text("All Sessions") }, onClick = {
                                viewModel.updateSelectedFilter("All")
                                showFilterMenu = false
                            })
                            DropdownMenuItem(text = { Text("Practice Only") }, onClick = {
                                viewModel.updateSelectedFilter("Practice")
                                showFilterMenu = false
                            })
                            DropdownMenuItem(text = { Text("Competition Only") }, onClick = {
                                viewModel.updateSelectedFilter("Competition")
                                showFilterMenu = false
                            })
                        }
                    }
                }, scrollBehavior = scrollBehavior
            )
        }
    }, floatingActionButton = {
        ExtendedFloatingActionButton(
            text = { Text("New Session") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Start Session") },
            onClick = { showNewSessionSheet = true },
            expanded = scrollBehavior.state.collapsedFraction < 0.5f
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
                        availableRounds.find { it.id == session.roundId }?.name
                            ?: "Unknown Round (ID: ${session.roundId})"

                    val stats by viewModel.getSessionStats(session.id)
                        .collectAsState(initial = Pair(0, 0f))

                    SessionItemCard(
                        session = session,
                        roundName = roundName,
                        totalScore = stats.first,
                        average = stats.second,
                        onClick = {
                            selectedSessionForSummary = session
                            viewModel.loadSessionSummary(session.id)
                        })
                }
            }
        }

        // --- 2. Overlays & Bottom Sheets ---
        if (showNewSessionSheet) {
            NewSessionDialog(
                rounds = availableRounds,
                savedLocations = savedLocations,
                savedBows = savedBowProfiles,
                savedArchers = savedArchers,
                savedArrowSets = savedArrowSets,
                defaultRoundId = userSettings.defaultRoundId,
                onSaveLocation = { viewModel.saveLocation(it) },
                onSaveBow = { viewModel.saveBowProfile(it) },
                onSaveArcher = { viewModel.saveArcher(it) },
                onSaveArrowSet = { viewModel.saveArrowSet(it) },
                onDismiss = { showNewSessionSheet = false },
                onStartSession = { roundId, type, method, archers, arrows, sessionName, bowName, locationName, archerName, arrowName ->
                    showNewSessionSheet = false
                    viewModel.startNewSession(
                        roundId, type, method, archers, arrows, sessionName, bowName, locationName, archerName, arrowName
                    ) { newSessionId ->
                        onNavigateToScoring(newSessionId)
                    }
                })
        }

        editingSession?.let { sessionToEdit ->
            NewSessionDialog(
                rounds = availableRounds,
                savedLocations = savedLocations,
                savedBows = savedBowProfiles,
                savedArchers = savedArchers,
                savedArrowSets = savedArrowSets,
                defaultRoundId = userSettings.defaultRoundId,
                onSaveLocation = { viewModel.saveLocation(it) },
                onSaveBow = { viewModel.saveBowProfile(it) },
                onSaveArcher = { viewModel.saveArcher(it) },
                onSaveArrowSet = { viewModel.saveArrowSet(it) },
                initialSession = sessionToEdit,
                onDismiss = { editingSession = null },
                onStartSession = { roundId, type, method, archers, arrows, sessionName, bowName, locationName, archerName, arrowName ->
                    viewModel.updateSessionDetails(
                        existingSession = sessionToEdit,
                        newRoundId = roundId,
                        newSessionType = type,
                        newInputMethod = method,
                        newArchers = archers,
                        newArrowsPerEnd = arrows,
                        newSessionName = sessionName,
                        newBowName = bowName,
                        newLocationName = locationName,
                        newArcherName = archerName,
                        newArrowName = arrowName,
                        onComplete = {
                            editingSession = null
                        }
                    )
                }
            )
        }

        selectedSessionForSummary?.let { sessionToSummarize ->
            val roundName =
                availableRounds.find { it.id == sessionToSummarize.roundId }?.name
                    ?: "Unknown Round"

            SessionSummaryBottomSheet(
                session = sessionToSummarize,
                roundName = roundName,
                summaryData = summaryData,
                onDismiss = {
                    selectedSessionForSummary = null
                    viewModel.clearSessionSummary()
                },
                onResumeClick = {
                    val idToResume = sessionToSummarize.id
                    selectedSessionForSummary = null
                    viewModel.clearSessionSummary()
                    onNavigateToScoring(idToResume)
                },
                onEditDetailsClick = {
                    editingSession = sessionToSummarize
                    selectedSessionForSummary = null
                    viewModel.clearSessionSummary()
                },
                onDeleteClick = {
                    sessionToDelete = sessionToSummarize
                    selectedSessionForSummary = null
                },
                onNotesClick = {
                    showNotesDialogFor = sessionToSummarize
                    selectedSessionForSummary = null
                })
        }
    }

    // Truncation Warning Confirmation Dialog
    truncationWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { viewModel.clearTruncationWarning() },
            title = {
                Text("Truncate Session Data?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(warning.message)
            },
            confirmButton = {
                Button(
                    onClick = { warning.onConfirm() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Truncate & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearTruncationWarning() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- 3. Dialogs ---
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