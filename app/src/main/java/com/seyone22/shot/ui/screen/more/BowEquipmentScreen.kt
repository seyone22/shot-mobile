package com.seyone22.shot.ui.screen.more

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.seyone22.shot.data.local.entity.BowProfileEntity
import com.seyone22.shot.data.local.entity.BowType
import com.seyone22.shot.di.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BowEquipmentScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToBowDetail: (Long) -> Unit, // 1. Add navigation callback
    viewModel: BowEquipmentViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bows by viewModel.bowProfiles.collectAsState()

    // State for the Add/Edit Bottom Sheet
    var showSheet by remember { mutableStateOf(false) }
    var bowToEdit by remember { mutableStateOf<BowProfileEntity?>(null) }

    // State for Delete Confirmation
    var bowToDelete by remember { mutableStateOf<BowProfileEntity?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("Bow Profiles") }, navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        })
    }, floatingActionButton = {
        FloatingActionButton(onClick = {
            bowToEdit = null // Ensure it's a blank slate for a new bow
            showSheet = true
        }) {
            Icon(Icons.Default.Add, contentDescription = "Add Bow")
        }
    }) { innerPadding ->
        if (bows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No bow profiles yet. Add one to get started!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(bows, key = { it.id }) { bow ->
                    BowProfileCard(bow = bow, onEdit = {
                        bowToEdit = bow
                        showSheet = true
                    }, onDelete = { bowToDelete = bow },
                        onClick = { onNavigateToBowDetail(bow.id) }, )
                }
            }
        }
    }

    // --- Bottom Sheet for Add/Edit ---
    if (showSheet) {
        BowProfileBottomSheet(
            initialBow = bowToEdit,
            onDismiss = { showSheet = false },
            onSave = { updatedBow ->
                viewModel.saveBowProfile(updatedBow)
                showSheet = false
            })
    }

    // --- Delete Confirmation Dialog ---
    bowToDelete?.let { bow ->
        AlertDialog(
            onDismissRequest = { bowToDelete = null },
            title = { Text("Delete ${bow.name}?") },
            text = { Text("This will permanently remove this bow profile. Note: If this bow is tied to past sessions, consider archiving it instead.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBowProfile(bow)
                        bowToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { bowToDelete = null }) { Text("Cancel") }
            })
    }
}

@Composable
fun BowProfileCard(
    bow: BowProfileEntity, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = bow.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (bow.isDefault) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = "Default",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = bow.bowType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Context Menu
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = {
                                expanded = false
                                onEdit()
                            })
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Delete,
                                    null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                expanded = false
                                onDelete()
                            })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Specs Row
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (bow.drawWeight != null) {
                    SpecItem("Draw Weight", "${bow.drawWeight} lbs")
                }
                if (bow.drawLength != null) {
                    SpecItem("Draw Length", "${bow.drawLength}\"")
                }
            }

            if (bow.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = bow.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BowProfileBottomSheet(
    initialBow: BowProfileEntity?, onDismiss: () -> Unit, onSave: (BowProfileEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialBow?.name ?: "") }
    var bowType by remember { mutableStateOf(initialBow?.bowType ?: BowType.RECURVE) }
    var drawWeight by remember { mutableStateOf(initialBow?.drawWeight?.toString() ?: "") }
    var drawLength by remember { mutableStateOf(initialBow?.drawLength?.toString() ?: "") }
    var notes by remember { mutableStateOf(initialBow?.notes ?: "") }
    var isDefault by remember { mutableStateOf(initialBow?.isDefault ?: false) }

    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialBow == null) "New Bow Profile" else "Edit Bow Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Profile Name (e.g. Indoor Setup)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Dropdown for Bow Type
            ExposedDropdownMenuBox(
                expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = bowType.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bow Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false }) {
                    BowType.entries.forEach { type ->
                        DropdownMenuItem(text = {
                            Text(
                                type.name.lowercase().replaceFirstChar { it.uppercase() })
                        }, onClick = {
                            bowType = type
                            expanded = false
                        })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = drawWeight,
                    onValueChange = { drawWeight = it },
                    label = { Text("Weight (lbs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = drawLength,
                    onValueChange = { drawLength = it },
                    label = { Text("Length (\")") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Tune settings") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDefault = !isDefault }
                    .padding(vertical = 8.dp)) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("Set as Default Bow")
            }

            Button(
                onClick = {
                    onSave(
                        BowProfileEntity(
                            id = initialBow?.id ?: 0L,
                            name = name,
                            bowType = bowType,
                            drawWeight = drawWeight.toFloatOrNull(),
                            drawLength = drawLength.toFloatOrNull(),
                            notes = notes,
                            isDefault = isDefault
                        )
                    )
                }, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank()
            ) {
                Text("Save Bow Profile")
            }
        }
    }
}