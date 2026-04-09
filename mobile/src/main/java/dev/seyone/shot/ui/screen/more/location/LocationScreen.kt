package dev.seyone.shot.ui.screen.more.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.seyone.core.domain.LocationType
import dev.seyone.core.domain.model.Location
import dev.seyone.shot.di.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val locations by viewModel.locations.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var locationToEdit by remember { mutableStateOf<Location?>(null) }
    var locationToDelete by remember { mutableStateOf<Location?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Ranges & Clubs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                locationToEdit = null
                showSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Location")
            }
        }
    ) { innerPadding ->
        if (locations.isEmpty()) {
            EmptyLocationState(Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier.fillMaxSize().padding(innerPadding)
            ) {
                items(locations, key = { it.id }) { location ->
                    LocationCard(
                        location = location,
                        onEdit = {
                            locationToEdit = location
                            showSheet = true
                        },
                        onDelete = { locationToDelete = location }
                    )
                }
            }
        }
    }

    if (showSheet) {
        LocationBottomSheet(
            initialLocation = locationToEdit,
            onDismiss = { showSheet = false },
            onSave = { viewModel.saveLocation(it); showSheet = false }
        )
    }

    // Delete Confirmation Dialog
    locationToDelete?.let { location ->
        AlertDialog(
            onDismissRequest = { locationToDelete = null },
            title = { Text("Delete ${location.name}?") },
            text = { Text("Sessions logged at this location will remain, but the location profile will be removed.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteLocation(location); locationToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { locationToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun LocationCard(
    location: Location,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(location.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    if (location.isDefault) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 8.dp).size(18.dp)
                        )
                    }
                }
                Text(
                    text = location.type.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                if (location.notes.isNotBlank()) {
                    Text(
                        location.notes,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Box {
                IconButton(onClick = { expanded = true }) { Icon(Icons.Default.MoreVert, null) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { expanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, onClick = { expanded = false; onDelete() })
                }
            }
        }
    }
}

@Composable
fun EmptyLocationState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Place,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No ranges tracked yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "Add your local club or home range to start scoring.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    initialLocation: Location?,
    onDismiss: () -> Unit,
    onSave: (Location) -> Unit
) {
    var name by remember { mutableStateOf(initialLocation?.name ?: "") }
    var type by remember { mutableStateOf(initialLocation?.type ?: LocationType.OUTDOOR) }
    var notes by remember { mutableStateOf(initialLocation?.notes ?: "") }
    var isDefault by remember { mutableStateOf(initialLocation?.isDefault ?: false) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (initialLocation == null) "New Location" else "Edit Location",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // --- Basic Info ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Location/Club Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Environment Selection ---
            Column {
                Text(
                    "Environment Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LocationType.entries.forEach { locationType ->
                        item{
                            FilterChip(
                                selected = type == locationType,
                                onClick = { type = locationType },
                                label = { Text(locationType.name) },
                                leadingIcon = if (type == locationType) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            // --- Additional Details ---
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Gate codes, lane rules, etc.)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // --- Default Toggle ---
            Surface(
                onClick = { isDefault = !isDefault },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Set as Default", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Automatically select this for new rounds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Actions ---
            Button(
                onClick = {
                    onSave(
                        Location(
                            id = initialLocation?.id ?: 0L,
                            name = name,
                            type = type,
                            isDefault = isDefault,
                            notes = notes
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Expressive large touch target
                enabled = name.isNotBlank()
            ) {
                Text("Save Location")
            }
        }
    }
}