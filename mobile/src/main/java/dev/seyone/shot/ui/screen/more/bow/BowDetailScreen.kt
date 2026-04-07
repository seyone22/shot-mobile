package dev.seyone.shot.ui.screen.more.bow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.seyone.shot.data.local.entity.BowComponentEntity
import dev.seyone.shot.di.AppViewModelProvider
import dev.seyone.shot.ui.screen.more.components.ComponentBottomSheet
import dev.seyone.shot.ui.screen.more.components.ComponentCard
import dev.seyone.shot.ui.screen.more.components.StatBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BowDetailScreen(
    bowId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditBow: (Long) -> Unit, // 1. Added edit navigation callback
    viewModel: BowDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bow by viewModel.bowProfile.collectAsState()
    val components by viewModel.components.collectAsState()

    var showComponentSheet by remember { mutableStateOf(false) }
    var componentToEdit by remember { mutableStateOf<BowComponentEntity?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    // 2. Added state for a delete confirmation dialog
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bow?.name ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Bow Actions")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit Bow Profile") },
                                onClick = {
                                    showMenu = false
                                    // 3. Trigger edit navigation
                                    bow?.id?.let { onNavigateToEditBow(it) }
                                },
                                leadingIcon = { Icon(Icons.Outlined.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Bow", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    // 4. Show the confirmation dialog instead of deleting immediately
                                    showDeleteConfirmDialog = true
                                },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                componentToEdit = null
                showComponentSheet = true
            }) {
                Icon(Icons.Default.Add, "Add Component")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Summary Stats Section ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBadge("Components", components.size.toString(), Modifier.weight(1f))
                    val totalCost = components.sumOf { it.price ?: 0.0 }
                    StatBadge("Total Cost", "$${String.format("%.2f", totalCost)}", Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                Text("Equipped Hardware", style = MaterialTheme.typography.titleMedium)
            }

            // --- Component List ---
            items(components) { component ->
                ComponentCard(
                    component = component,
                    onEdit = {
                        componentToEdit = component
                        showComponentSheet = true
                    }
                )
            }
        }
    }

    // 5. Added the Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete ${bow?.name ?: "Bow"}?") },
            text = { Text("Are you sure you want to delete this bow profile? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // You will need to add a delete function to your ViewModel!
                        viewModel.deleteBowProfile(bowId)
                        showDeleteConfirmDialog = false
                        onNavigateBack() // Return to the previous screen since this bow is gone
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showComponentSheet) {
        ComponentBottomSheet(
            bowId = bowId, // Using the Composable parameter directly as discussed previously
            initialComponent = componentToEdit,
            onDismiss = { showComponentSheet = false },
            onSave = {
                viewModel.saveComponent(it)
                showComponentSheet = false // 6. Close the sheet after saving
            }
        )
    }
}