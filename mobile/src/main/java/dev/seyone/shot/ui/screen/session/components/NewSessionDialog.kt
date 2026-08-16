package dev.seyone.shot.ui.screen.session.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.ScoringMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.ShootingType
import dev.seyone.core.domain.TargetFaceSize
import dev.seyone.core.domain.model.Distance
import dev.seyone.core.domain.model.Round
import dev.seyone.core.domain.model.Session
import dev.seyone.core.domain.model.Archer
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.core.domain.model.BowProfile
import dev.seyone.core.domain.model.Location
import dev.seyone.shot.ui.screen.more.arrow.ArrowSetBottomSheet
import dev.seyone.shot.ui.screen.more.bow.BowProfileBottomSheet
import dev.seyone.shot.ui.screen.more.components.AddArcherDialog
import dev.seyone.shot.ui.screen.more.location.LocationBottomSheet
import dev.seyone.shot.ui.theme.ShotTheme

@Composable
fun NewSessionDialog(
    rounds: List<Round>,
    savedLocations: List<Location> = emptyList(),
    savedBows: List<BowProfile> = emptyList(),
    savedArchers: List<Archer> = emptyList(),
    savedArrowSets: List<ArrowSet> = emptyList(),
    defaultRoundId: Long = 1L,
    initialSession: Session? = null,
    onSaveLocation: ((Location) -> Unit)? = null,
    onSaveBow: ((BowProfile) -> Unit)? = null,
    onSaveArcher: ((Archer) -> Unit)? = null,
    onSaveArrowSet: ((ArrowSet) -> Unit)? = null,
    onDismiss: () -> Unit,
    onStartSession: (
        roundId: Long,
        sessionType: SessionType,
        inputMethod: InputMethod,
        archers: Int,
        arrowsPerEnd: Int,
        sessionName: String,
        bowName: String,
        locationName: String,
        archerName: String,
        arrowName: String
    ) -> Unit
) {
    if (LocalInspectionMode.current) {
        NewSessionDialogContent(
            rounds = rounds,
            savedLocations = savedLocations,
            savedBows = savedBows,
            savedArchers = savedArchers,
            savedArrowSets = savedArrowSets,
            defaultRoundId = defaultRoundId,
            initialSession = initialSession,
            onSaveLocation = onSaveLocation,
            onSaveBow = onSaveBow,
            onSaveArcher = onSaveArcher,
            onSaveArrowSet = onSaveArrowSet,
            onDismiss = onDismiss,
            onStartSession = onStartSession
        )
    } else {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = true
            )
        ) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect {
                dialogWindow?.let { window ->
                    window.setDimAmount(0f)
                    window.setWindowAnimations(-1)
                }
            }

            NewSessionDialogContent(
                rounds = rounds,
                savedLocations = savedLocations,
                savedBows = savedBows,
                savedArchers = savedArchers,
                savedArrowSets = savedArrowSets,
                defaultRoundId = defaultRoundId,
                initialSession = initialSession,
                onSaveLocation = onSaveLocation,
                onSaveBow = onSaveBow,
                onSaveArcher = onSaveArcher,
                onSaveArrowSet = onSaveArrowSet,
                onDismiss = onDismiss,
                onStartSession = onStartSession
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewSessionDialogContent(
    rounds: List<Round>,
    savedLocations: List<Location> = emptyList(),
    savedBows: List<BowProfile> = emptyList(),
    savedArchers: List<Archer> = emptyList(),
    savedArrowSets: List<ArrowSet> = emptyList(),
    defaultRoundId: Long = 1L,
    initialSession: Session? = null,
    onSaveLocation: ((Location) -> Unit)? = null,
    onSaveBow: ((BowProfile) -> Unit)? = null,
    onSaveArcher: ((Archer) -> Unit)? = null,
    onSaveArrowSet: ((ArrowSet) -> Unit)? = null,
    onDismiss: () -> Unit,
    onStartSession: (
        roundId: Long,
        sessionType: SessionType,
        inputMethod: InputMethod,
        archers: Int,
        arrowsPerEnd: Int,
        sessionName: String,
        bowName: String,
        locationName: String,
        archerName: String,
        arrowName: String
    ) -> Unit
) {
    // Dynamic rounds list state to support on-the-fly custom round creation
    var availableRounds by remember(rounds) { mutableStateOf(rounds) }
    var showCustomRoundDialog by remember { mutableStateOf(false) }

    val categories = remember(availableRounds) {
        val extracted = availableRounds.map { it.category }.distinct().filter { it.isNotBlank() }
        val order = listOf("WA (Outdoor)", "WA (Indoor)", "NFAA / USA Archery", "Custom")
        (order.filter { it in extracted || it == "Custom" } + extracted.filter { it !in order }).distinct()
    }

    val targetRoundId = initialSession?.roundId ?: defaultRoundId
    val initialRound = remember(availableRounds, targetRoundId) {
        availableRounds.find { it.id == targetRoundId } ?: availableRounds.firstOrNull()
    }
    var selectedCategory by remember(initialRound) { mutableStateOf(initialRound?.category ?: "WA (Outdoor)") }
    val filteredRounds = remember(availableRounds, selectedCategory) {
        val matches = availableRounds.filter { it.category == selectedCategory }
        matches.ifEmpty { availableRounds }
    }
    var selectedRound by remember(filteredRounds, initialRound) {
        mutableStateOf<Round?>(initialRound ?: filteredRounds.firstOrNull())
    }

    LaunchedEffect(selectedCategory) {
        if (selectedRound != null && filteredRounds.none { it.id == selectedRound?.id }) {
            selectedRound = filteredRounds.firstOrNull()
        }
    }

    var sessionName by remember(initialSession) { mutableStateOf(initialSession?.notes ?: "") }
    var sessionType by remember(initialSession) { mutableStateOf(initialSession?.sessionType ?: SessionType.PRACTICE) }
    var inputMethod by remember(initialSession) { mutableStateOf(initialSession?.inputMethod ?: InputMethod.ARROW_VALUES) }
    var numberOfArchers by remember(initialSession) { mutableIntStateOf(initialSession?.numberOfArchers ?: 1) }
    var arrowsPerEnd by remember(initialSession) { mutableIntStateOf(initialSession?.arrowsPerEnd ?: 6) }

    val availableBows = remember(savedBows) {
        savedBows.map { it.name }
    }
    val defaultBow = remember(savedBows) {
        savedBows.find { it.isDefault }?.name ?: savedBows.firstOrNull()?.name ?: ""
    }

    val availableLocations = remember(savedLocations) {
        savedLocations.map { it.name }
    }
    val defaultLocation = remember(savedLocations) {
        savedLocations.find { it.isDefault }?.name ?: savedLocations.firstOrNull()?.name ?: ""
    }

    val initialBowName = remember(initialSession, savedBows, defaultBow) {
        if (initialSession?.bowId != null) {
            savedBows.find { it.id == initialSession.bowId }?.name ?: defaultBow
        } else defaultBow
    }

    val initialLocationName = remember(initialSession, savedLocations, defaultLocation) {
        if (initialSession?.locationId != null) {
            savedLocations.find { it.id == initialSession.locationId }?.name ?: defaultLocation
        } else defaultLocation
    }

    val availableArchers = remember(savedArchers) {
        savedArchers.map { it.name }
    }
    val defaultArcher = remember(savedArchers) {
        savedArchers.firstOrNull()?.name ?: ""
    }

    val initialArcherName = remember(initialSession, savedArchers, defaultArcher) {
        if (initialSession?.archerId != null) {
            savedArchers.find { it.id == initialSession.archerId }?.name ?: defaultArcher
        } else defaultArcher
    }

    val availableArrowSets = remember(savedArrowSets) {
        savedArrowSets.map { it.name }
    }
    val defaultArrowSet = remember(savedArrowSets) {
        savedArrowSets.find { it.isDefault }?.name ?: savedArrowSets.firstOrNull()?.name ?: ""
    }

    val initialArrowName = remember(initialSession, savedArrowSets, defaultArrowSet) {
        if (initialSession?.arrowId != null) {
            savedArrowSets.find { it.id == initialSession.arrowId }?.name ?: defaultArrowSet
        } else defaultArrowSet
    }

    var selectedBow by remember(initialBowName) { mutableStateOf(initialBowName) }
    var selectedLocation by remember(initialLocationName) { mutableStateOf(initialLocationName) }
    var selectedArcher by remember(initialArcherName) { mutableStateOf(initialArcherName) }
    var selectedArrowSet by remember(initialArrowName) { mutableStateOf(initialArrowName) }
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    // State for Bottom Sheet Modal Picker
    var showRoundPickerSheet by remember { mutableStateOf(false) }
    var showCreateLocationSheet by remember { mutableStateOf(false) }
    var showCreateBowSheet by remember { mutableStateOf(false) }
    var showCreateArcherSheet by remember { mutableStateOf(false) }
    var showCreateArrowSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (initialSession != null) "Edit Session" else "New Session",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            selectedRound?.let {
                                onStartSession(
                                    it.id,
                                    sessionType,
                                    inputMethod,
                                    numberOfArchers,
                                    arrowsPerEnd,
                                    sessionName,
                                    selectedBow,
                                    selectedLocation,
                                    selectedArcher,
                                    selectedArrowSet
                                )
                            }
                        },
                        enabled = selectedRound != null
                    ) {
                        Text(
                            if (initialSession != null) "Save" else "Start",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    selectedRound?.let {
                        onStartSession(
                            it.id,
                            sessionType,
                            inputMethod,
                            numberOfArchers,
                            arrowsPerEnd,
                            sessionName,
                            selectedBow,
                            selectedLocation,
                            selectedArcher,
                            selectedArrowSet
                        )
                    }
                },
                expanded = true,
                icon = { Icon(if (initialSession != null) Icons.Default.Check else Icons.Default.PlayArrow, contentDescription = null) },
                text = {
                    Text(
                        text = if (initialSession != null) "Save Session" else "Start Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ==========================================
            // CARD 1: ROUND SELECTION & CATEGORY CHIPS
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Rulebook & Round",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // 1. Horizontal Scrollable Category Filter Chips
                    Text(
                        text = "Rulebook Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = category == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = category },
                                label = {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else if (category == "Custom") {
                                    { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. Rich Round Picker Selector Card Button
                    Text(
                        text = "Active Round",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    RoundSelectorCardButton(
                        selectedRound = selectedRound,
                        onClick = { showRoundPickerSheet = true }
                    )

                    // 3. Live Details Preview
                    selectedRound?.let { round ->
                        RoundDetailsPreview(round = round)
                    }
                }
            }

            // ==========================================
            // CARD 2: SCORING & SESSION CONFIGURATION
            // ==========================================
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Scoring Setup",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Session Mode (Practice vs Competition)
                    ScoringChoiceRow(
                        label = "Session Mode",
                        options = listOf(
                            ChoiceOption("Practice", Icons.Default.FitnessCenter),
                            ChoiceOption("Competition", Icons.Default.EmojiEvents)
                        ),
                        selectedIndex = if (sessionType == SessionType.PRACTICE) 0 else 1,
                        onSelect = {
                            sessionType = if (it == 0) SessionType.PRACTICE else SessionType.COMPETITION
                        }
                    )

                    // Input Mode (Keypad Grid vs Target Face)
                    ScoringChoiceRow(
                        label = "Input Mode",
                        options = listOf(
                            ChoiceOption("Keypad Grid", Icons.Default.Calculate),
                            ChoiceOption("Target Face", Icons.Default.AdsClick)
                        ),
                        selectedIndex = if (inputMethod == InputMethod.ARROW_VALUES) 0 else 1,
                        onSelect = {
                            inputMethod = if (it == 0) InputMethod.ARROW_VALUES else InputMethod.TARGET_FACE
                        }
                    )

                    // Arrows per End
                    ScoringChoiceRow(
                        label = "Arrows per End",
                        options = listOf(
                            ChoiceOption("3 Arrows", Icons.Default.Repeat),
                            ChoiceOption("6 Arrows", Icons.Default.Repeat)
                        ),
                        selectedIndex = if (arrowsPerEnd == 3) 0 else 1,
                        onSelect = { arrowsPerEnd = if (it == 0) 3 else 6 }
                    )

                }
            }

            // ==========================================
            // CARD 3: COLLAPSIBLE ADDITIONAL DETAILS (OPTIONAL)
            // ==========================================
            Card(
                onClick = { isAdvancedExpanded = !isAdvancedExpanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Additional Details (Optional)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            if (!isAdvancedExpanded) {
                                val summaryLine = remember(sessionName, selectedArcher, selectedBow, selectedArrowSet, selectedLocation, numberOfArchers) {
                                    listOfNotNull(
                                        sessionName.ifBlank { null },
                                        selectedArcher.ifBlank { null },
                                        selectedBow.ifBlank { null },
                                        selectedArrowSet.ifBlank { null },
                                        selectedLocation.ifBlank { null },
                                        "$numberOfArchers Archer(s)"
                                    ).joinToString(" • ")
                                }
                                Text(
                                    text = summaryLine,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isAdvancedExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isAdvancedExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isAdvancedExpanded) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        OutlinedTextField(
                            value = sessionName,
                            onValueChange = { sessionName = it },
                            label = { Text("Session / Event Name (Optional)") },
                            placeholder = { Text("e.g. National Selection Trial 1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChipSelectionRow(
                            label = "Archer / Shooter",
                            options = availableArchers,
                            selectedOption = selectedArcher,
                            onOptionSelected = { selectedArcher = it },
                            emptyActionLabel = "Add Archer Profile",
                            onEmptyActionClick = { showCreateArcherSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChipSelectionRow(
                            label = "Bow / Equipment Setup",
                            options = availableBows,
                            selectedOption = selectedBow,
                            onOptionSelected = { selectedBow = it },
                            emptyActionLabel = "Add Bow Profile",
                            onEmptyActionClick = { showCreateBowSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChipSelectionRow(
                            label = "Arrow Set / Shafts",
                            options = availableArrowSets,
                            selectedOption = selectedArrowSet,
                            onOptionSelected = { selectedArrowSet = it },
                            emptyActionLabel = "Add Arrow Profile",
                            onEmptyActionClick = { showCreateArrowSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        ChipSelectionRow(
                            label = "Shooting Location / Range",
                            options = availableLocations,
                            selectedOption = selectedLocation,
                            onOptionSelected = { selectedLocation = it },
                            emptyActionLabel = "Add Range / Club",
                            onEmptyActionClick = { showCreateLocationSheet = true },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Archers Count
                        ScoringChoiceRow(
                            label = "Number of Archers",
                            options = listOf(
                                ChoiceOption("1", Icons.Default.Group),
                                ChoiceOption("2", Icons.Default.Group),
                                ChoiceOption("3", Icons.Default.Group),
                                ChoiceOption("4", Icons.Default.Group)
                            ),
                            selectedIndex = numberOfArchers - 1,
                            onSelect = { numberOfArchers = it + 1 }
                        )
                    }
                }
            }

            // Extra whitespace padding at bottom so FAB never obscures interactive elements when scrolled
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    // ==========================================
    // SEARCHABLE ROUND PICKER BOTTOM SHEET
    // ==========================================
    if (showRoundPickerSheet) {
        RoundPickerBottomSheet(
            rounds = availableRounds,
            categories = categories,
            selectedCategory = selectedCategory,
            selectedRound = selectedRound,
            onSelectRound = { round ->
                selectedRound = round
                selectedCategory = round.category
                showRoundPickerSheet = false
            },
            onAddCustomRound = { newCustomRound ->
                availableRounds = availableRounds + newCustomRound
                selectedRound = newCustomRound
                selectedCategory = "Custom"
                showRoundPickerSheet = false
            },
            onDismiss = { showRoundPickerSheet = false }
        )
    }

    if (showCreateLocationSheet) {
        LocationBottomSheet(
            initialLocation = null,
            onDismiss = { showCreateLocationSheet = false },
            onSave = { newLocation ->
                showCreateLocationSheet = false
                onSaveLocation?.invoke(newLocation)
                selectedLocation = newLocation.name
            }
        )
    }

    if (showCreateBowSheet) {
        BowProfileBottomSheet(
            initialBow = null,
            onDismiss = { showCreateBowSheet = false },
            onSave = { newBow ->
                onSaveBow?.invoke(newBow)
                selectedBow = newBow.name
                showCreateBowSheet = false
            }
        )
    }

    if (showCreateArrowSheet) {
        ArrowSetBottomSheet(
            initialArrowSet = null,
            onDismiss = { showCreateArrowSheet = false },
            onSave = { newArrow ->
                onSaveArrowSet?.invoke(newArrow)
                selectedArrowSet = newArrow.name
                showCreateArrowSheet = false
            }
        )
    }

    if (showCreateArcherSheet) {
        AddArcherDialog(
            onDismiss = { showCreateArcherSheet = false },
            onConfirm = { name, club, gender, ageGroup ->
                showCreateArcherSheet = false
                val newArcher = Archer(name = name, clubName = club, gender = gender, ageGroup = ageGroup)
                onSaveArcher?.invoke(newArcher)
                selectedArcher = name
            }
        )
    }
}

// --- ROUND SELECTOR CARD BUTTON COMPONENT ---
@Composable
private fun RoundSelectorCardButton(
    selectedRound: Round?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedRound?.name ?: "Select Round",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = selectedRound?.category ?: "Tap to choose competition round",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Choose Round",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// --- SEARCHABLE ROUND PICKER BOTTOM SHEET ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RoundPickerBottomSheet(
    rounds: List<Round>,
    categories: List<String>,
    selectedCategory: String,
    selectedRound: Round?,
    onSelectRound: (Round) -> Unit,
    onAddCustomRound: (Round) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf(selectedCategory) }
    var showCreateCustomDialog by remember { mutableStateOf(false) }

    val filteredRounds = remember(rounds, searchQuery, activeCategoryFilter) {
        rounds.filter { round ->
            val matchesCategory = activeCategoryFilter.isEmpty() || round.category == activeCategoryFilter
            val matchesQuery = searchQuery.isEmpty() ||
                    round.name.contains(searchQuery, ignoreCase = true) ||
                    round.category.contains(searchQuery, ignoreCase = true) ||
                    round.distances.any { "${it.distanceValue}m".contains(searchQuery, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Competition Round",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search rounds by name or distance...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = activeCategoryFilter.isEmpty(),
                        onClick = { activeCategoryFilter = "" },
                        label = { Text("All Rounds") }
                    )
                }
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == activeCategoryFilter,
                        onClick = { activeCategoryFilter = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Rounds List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // "Create Custom Round" Action Button at top of list
                item {
                    OutlinedButton(
                        onClick = { showCreateCustomDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Create Custom Round",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                items(filteredRounds) { round ->
                    val isSelected = round.id == selectedRound?.id
                    val totalArrows = round.distances.sumOf { it.arrowsPerEnd * it.numberOfEnds }

                    Surface(
                        onClick = { onSelectRound(round) },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = round.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = round.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (totalArrows > 0) "$totalArrows Arrows" else "Custom",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (round.distances.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    round.distances.forEach { d ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Text(
                                                text = "${d.distanceValue}${d.distanceUnit.symbol} (${d.targetFaceSize.description})",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredRounds.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No rounds match your search",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateCustomDialog) {
        CreateCustomRoundDialog(
            onDismiss = { showCreateCustomDialog = false },
            onSaveRound = { newRound ->
                showCreateCustomDialog = false
                onAddCustomRound(newRound)
            }
        )
    }
}

// --- CREATE CUSTOM ROUND DIALOG ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCustomRoundDialog(
    onDismiss: () -> Unit,
    onSaveRound: (Round) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var distanceValueText by remember { mutableStateOf("30") }
    var distanceUnit by remember { mutableStateOf(DistanceUnit.METERS) }
    var numberOfEndsText by remember { mutableStateOf("6") }
    var arrowsPerEndText by remember { mutableStateOf("6") }
    var selectedFaceSize by remember { mutableStateOf(TargetFaceSize.CM_80) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Round", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Round Name") },
                    placeholder = { Text("e.g. 30m Sight Tuning") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = distanceValueText,
                        onValueChange = { distanceValueText = it.filter { char -> char.isDigit() } },
                        label = { Text("Distance") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Unit", style = MaterialTheme.typography.labelSmall)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = distanceUnit == DistanceUnit.METERS,
                                onClick = { distanceUnit = DistanceUnit.METERS },
                                shape = SegmentedButtonDefaults.itemShape(0, 2),
                                icon = {},
                                label = {
                                    Text(
                                        "Meters",
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                            SegmentedButton(
                                selected = distanceUnit == DistanceUnit.YARDS,
                                onClick = { distanceUnit = DistanceUnit.YARDS },
                                shape = SegmentedButtonDefaults.itemShape(1, 2),
                                icon = {},
                                label = {
                                    Text(
                                        "Yards",
                                        maxLines = 1,
                                        softWrap = false,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = numberOfEndsText,
                        onValueChange = { numberOfEndsText = it.filter { char -> char.isDigit() } },
                        label = { Text("Ends") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = arrowsPerEndText,
                        onValueChange = { arrowsPerEndText = it.filter { char -> char.isDigit() } },
                        label = { Text("Arrows / End") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Target Face Size", style = MaterialTheme.typography.labelSmall)
                DropdownField(
                    label = "Target Face",
                    options = TargetFaceSize.entries.map { it.description },
                    selectedOption = selectedFaceSize.description,
                    onOptionSelected = { desc ->
                        selectedFaceSize = TargetFaceSize.entries.find { it.description == desc } ?: TargetFaceSize.CM_80
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val distVal = distanceValueText.toIntOrNull() ?: 30
                    val endsVal = numberOfEndsText.toIntOrNull() ?: 6
                    val arrowsVal = arrowsPerEndText.toIntOrNull() ?: 6
                    val roundName = if (name.isNotBlank()) name else "Custom ${distVal}${distanceUnit.symbol}"

                    val newCustomRound = Round(
                        id = System.currentTimeMillis(),
                        name = roundName,
                        category = "Custom",
                        scoringMethod = ScoringMethod.METRIC_10_ZONE,
                        shootingType = ShootingType.TARGET,
                        distances = listOf(
                            Distance(
                                id = System.currentTimeMillis(),
                                roundId = System.currentTimeMillis(),
                                sequenceOrder = 1,
                                distanceValue = distVal,
                                distanceUnit = distanceUnit,
                                arrowsPerEnd = arrowsVal,
                                numberOfEnds = endsVal,
                                targetFaceSize = selectedFaceSize
                            )
                        ),
                        isCustom = true
                    )
                    onSaveRound(newCustomRound)
                },
                enabled = distanceValueText.isNotBlank()
            ) {
                Text("Save & Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// --- LIVE ROUND DETAILS PREVIEW COMPONENT ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoundDetailsPreview(round: Round) {
    val totalArrows = remember(round) {
        round.distances.sumOf { it.arrowsPerEnd * it.numberOfEnds }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Round Overview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (totalArrows > 0) "$totalArrows Arrows Total" else "Custom Round",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Distance & Target Chips (Single Horizontal Row)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(round.distances) { dist ->
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            "${dist.distanceValue}${dist.distanceUnit.symbol} • ${dist.targetFaceSize.description}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
            if (round.distances.isEmpty()) {
                item {
                    AssistChip(
                        onClick = { },
                        label = { Text("Standard Scoring Rule", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

// --- OPTION CHOICE ROW WITH MATERIAL ICONS ---
private data class ChoiceOption(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun ScoringChoiceRow(
    label: String,
    options: List<ChoiceOption>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    icon = {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(option.label, style = MaterialTheme.typography.labelMedium)
                    }
                )
            }
        }
    }
}

// --- DROPDOWN FIELD COMPONENT (Reused by AddArcherDialog) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ChipSelectionRow(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    emptyActionLabel: String? = null,
    onEmptyActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (options.isEmpty()) {
            if (emptyActionLabel != null && onEmptyActionClick != null) {
                FilterChip(
                    selected = false,
                    onClick = onEmptyActionClick,
                    label = { Text(emptyActionLabel) },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )
            } else {
                Text(
                    text = "None saved yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options) { opt ->
                    val isSelected = opt == selectedOption
                    FilterChip(
                        selected = isSelected,
                        onClick = { onOptionSelected(opt) },
                        label = { Text(opt) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// COMPOSE DESIGN PREVIEW COMPOSABLE
// ==========================================
@Preview(showBackground = true, widthDp = 390, heightDp = 840)
@Composable
fun NewSessionDialogPreview() {
    val sampleRounds = listOf(
        Round(
            id = 1,
            name = "WA 1440 (90m)",
            category = "WA (Outdoor)",
            scoringMethod = ScoringMethod.METRIC_10_ZONE,
            shootingType = ShootingType.TARGET,
            distances = listOf(
                Distance(1, 1, 1, 90, DistanceUnit.METERS, 6, 6, TargetFaceSize.CM_122),
                Distance(2, 1, 2, 70, DistanceUnit.METERS, 6, 6, TargetFaceSize.CM_122),
                Distance(3, 1, 3, 50, DistanceUnit.METERS, 6, 6, TargetFaceSize.CM_80),
                Distance(4, 1, 4, 30, DistanceUnit.METERS, 6, 6, TargetFaceSize.CM_80)
            )
        ),
        Round(
            id = 2,
            name = "WA 70m (Recurve)",
            category = "WA (Outdoor)",
            scoringMethod = ScoringMethod.METRIC_10_ZONE,
            shootingType = ShootingType.TARGET,
            distances = listOf(
                Distance(5, 2, 1, 70, DistanceUnit.METERS, 6, 12, TargetFaceSize.CM_122)
            )
        ),
        Round(
            id = 3,
            name = "WA 18m (Indoor)",
            category = "WA (Indoor)",
            scoringMethod = ScoringMethod.METRIC_10_ZONE,
            shootingType = ShootingType.TARGET,
            distances = listOf(
                Distance(6, 3, 1, 18, DistanceUnit.METERS, 3, 20, TargetFaceSize.CM_40)
            )
        ),
        Round(
            id = 4,
            name = "Vegas 300",
            category = "NFAA / USA Archery",
            scoringMethod = ScoringMethod.METRIC_10_ZONE,
            shootingType = ShootingType.TARGET,
            distances = listOf(
                Distance(7, 4, 1, 18, DistanceUnit.METERS, 3, 10, TargetFaceSize.CM_40_TRIPLE)
            )
        )
    )

    ShotTheme {
        NewSessionDialogContent(
            rounds = sampleRounds,
            onDismiss = {},
            onStartSession = { _, _, _, _, _, _, _, _, _, _ -> }
        )
    }
}