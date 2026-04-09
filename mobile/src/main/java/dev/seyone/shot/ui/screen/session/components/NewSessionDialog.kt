package dev.seyone.shot.ui.screen.session.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.model.Round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSessionDialog(
    rounds: List<Round>, onDismiss: () -> Unit, onStartSession: (
        roundId: Long, sessionType: SessionType, inputMethod: InputMethod, archers: Int, arrowsPerEnd: Int
    ) -> Unit
) {
    // --- State Management ---
    val categories = remember(rounds) { rounds.map { it.category }.distinct() }
    var selectedCategory by remember(categories) { mutableStateOf(categories.firstOrNull() ?: "") }
    val filteredRounds = remember(
        rounds, selectedCategory
    ) { rounds.filter { it.category == selectedCategory } }
    var selectedRound by remember { mutableStateOf<Round?>(null) }

    LaunchedEffect(selectedCategory) { selectedRound = filteredRounds.firstOrNull() }

    var sessionType by remember { mutableStateOf(SessionType.PRACTICE) }
    var inputMethod by remember { mutableStateOf(InputMethod.TARGET_FACE) }
    var numberOfArchers by remember { mutableIntStateOf(1) }
    var arrowsPerEnd by remember { mutableIntStateOf(6) }

    Dialog(
        onDismissRequest = onDismiss, properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true // Ensures it draws behind status bars if needed
        )
    ) {
        // --- DISABLE DIALOG SCRIM (Darkening) ---
        val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            dialogWindow?.let { window ->
                window.setDimAmount(0f) // This removes the background dimming
                window.setWindowAnimations(-1) // Optional: cleaner transition
            }
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("New Session", style = MaterialTheme.typography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                selectedRound?.let {
                                    onStartSession(
                                        it.id,
                                        sessionType,
                                        inputMethod,
                                        numberOfArchers,
                                        arrowsPerEnd
                                    )
                                }
                            },
                            enabled = selectedRound != null,
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Start")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        // EXPLICITLY SET CONTENT COLORS
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SectionHeader("Round Configuration")
                DropdownField(
                    label = "Rulebook / Category",
                    options = categories,
                    selectedOption = selectedCategory,
                    onOptionSelected = { selectedCategory = it },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownField(
                    label = "Round",
                    options = filteredRounds.map { it.name },
                    selectedOption = selectedRound?.name ?: "Select a round",
                    onOptionSelected = { name ->
                        selectedRound = filteredRounds.find { it.name == name }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                SectionHeader("Scoring Details")

                ScoringChoiceRow(
                    label = "Session Type",
                    options = listOf("Practice", "Competition"),
                    selectedIndex = if (sessionType == SessionType.PRACTICE) 0 else 1,
                    onSelect = {
                        sessionType = if (it == 0) SessionType.PRACTICE else SessionType.COMPETITION
                    })

                ScoringChoiceRow(
                    label = "Input Method",
                    options = listOf("Target Face", "Arrow Values"),
                    selectedIndex = if (inputMethod == InputMethod.TARGET_FACE) 0 else 1,
                    onSelect = {
                        inputMethod =
                            if (it == 0) InputMethod.TARGET_FACE else InputMethod.ARROW_VALUES
                    })

                ScoringChoiceRow(
                    label = "Arrows per End",
                    options = listOf("3 Arrows", "6 Arrows"),
                    selectedIndex = if (arrowsPerEnd == 3) 0 else 1,
                    onSelect = { arrowsPerEnd = if (it == 0) 3 else 6 })

                ScoringChoiceRow(
                    label = "Number of Archers",
                    options = listOf("1", "2", "3", "4"),
                    selectedIndex = numberOfArchers - 1,
                    onSelect = { numberOfArchers = it + 1 })

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- Helper Components remain the same ---
@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ScoringChoiceRow(
    label: String, options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selectedIndex == index,
                    onClick = { onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                ) { Text(option) }
            }
        }
    }
}

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
        expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}