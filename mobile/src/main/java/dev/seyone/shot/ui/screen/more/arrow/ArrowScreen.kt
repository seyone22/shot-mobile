package dev.seyone.shot.ui.screen.more.arrow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.seyone.core.domain.model.ArrowSet
import dev.seyone.shot.di.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrowEquipmentScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArrowEquipmentViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val arrowSets by viewModel.arrowSets.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var arrowSetToEdit by remember { mutableStateOf<ArrowSet?>(null) }
    var arrowSetToDelete by remember { mutableStateOf<ArrowSet?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arrow Inventory") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                arrowSetToEdit = null
                showSheet = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Arrow Set")
            }
        }
    ) { innerPadding ->
        if (arrowSets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No arrows tracked yet. Add a set to begin!",
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
                items(arrowSets, key = { it.id }) { arrowSet ->
                    ArrowSetCard(
                        arrowSet = arrowSet,
                        onEdit = {
                            arrowSetToEdit = arrowSet
                            showSheet = true
                        },
                        onDelete = { arrowSetToDelete = arrowSet }
                    )
                }
            }
        }
    }

    if (showSheet) {
        ArrowSetBottomSheet(
            initialArrowSet = arrowSetToEdit,
            onDismiss = { showSheet = false },
            onSave = { updatedArrowSet ->
                viewModel.saveArrowSet(updatedArrowSet)
                showSheet = false
            }
        )
    }

    arrowSetToDelete?.let { arrowSet ->
        AlertDialog(
            onDismissRequest = { arrowSetToDelete = null },
            title = { Text("Delete ${arrowSet.name}?") },
            text = { Text("This will permanently remove this arrow set from your inventory. If you've shot rounds with these arrows, consider archiving them instead to keep your stats intact.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteArrowSet(arrowSet)
                        arrowSetToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { arrowSetToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ArrowSetCard(
    arrowSet: ArrowSet,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = arrowSet.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (arrowSet.isDefault) {
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
                        text = listOf(arrowSet.manufacturer, arrowSet.model).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit Specs") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = {
                                expanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Arrow Specs Grid
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                arrowSet.spine?.let { SpecItem("Spine", it.toString()) }
                arrowSet.lengthInches?.let { SpecItem("Length", "$it\"") }
                arrowSet.weightGrains?.let { SpecItem("Weight", "${it}gn") }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Usage & Inventory Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    SpecItem("Inventory", "${arrowSet.quantity} arrows", isHighlight = arrowSet.quantity < 6)
                    SpecItem("Total Shots", "${arrowSet.shotCount}")
                }

                if (arrowSet.purchasePrice != null) {
                    Text(
                        text = "$${arrowSet.purchasePrice}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (arrowSet.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = arrowSet.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpecItem(label: String, value: String, isHighlight: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrowSetBottomSheet(
    initialArrowSet: ArrowSet?,
    onDismiss: () -> Unit,
    onSave: (ArrowSet) -> Unit
) {
    var name by remember { mutableStateOf(initialArrowSet?.name ?: "") }
    var manufacturer by remember { mutableStateOf(initialArrowSet?.manufacturer ?: "") }
    var model by remember { mutableStateOf(initialArrowSet?.model ?: "") }
    var spine by remember { mutableStateOf(initialArrowSet?.spine?.toString() ?: "") }
    var length by remember { mutableStateOf(initialArrowSet?.lengthInches?.toString() ?: "") }
    var weight by remember { mutableStateOf(initialArrowSet?.weightGrains?.toString() ?: "") }
    var quantity by remember { mutableStateOf(initialArrowSet?.quantity?.toString() ?: "12") }
    var price by remember { mutableStateOf(initialArrowSet?.purchasePrice?.toString() ?: "") }
    var notes by remember { mutableStateOf(initialArrowSet?.notes ?: "") }
    var isDefault by remember { mutableStateOf(initialArrowSet?.isDefault ?: false) }

    val scrollState = rememberScrollState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialArrowSet == null) "New Arrow Set" else "Edit Arrow Set",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // --- General Info ---
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Set Name (e.g., Competition X10s)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = manufacturer,
                    onValueChange = { manufacturer = it },
                    label = { Text("Brand") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // --- Specifications ---
            Text("Specifications", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = spine,
                    onValueChange = { spine = it },
                    label = { Text("Spine") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = length,
                    onValueChange = { length = it },
                    label = { Text("Length (\")") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Weight (gn)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // --- Inventory & Purchase ---
            Text("Inventory", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Arrow Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Total Cost ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Fletching type, damage, etc.)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDefault = !isDefault }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("Set as Default Arrow Set")
            }

            Button(
                onClick = {
                    onSave(
                        ArrowSet(
                            id = initialArrowSet?.id ?: 0L,
                            name = name,
                            manufacturer = manufacturer,
                            model = model,
                            spine = spine.toIntOrNull(),
                            lengthInches = length.toFloatOrNull(),
                            weightGrains = weight.toFloatOrNull(),
                            quantity = quantity.toIntOrNull() ?: 0,
                            shotCount = initialArrowSet?.shotCount ?: 0, // Preserve shot count on edit
                            purchasePrice = price.toDoubleOrNull(),
                            notes = notes,
                            isDefault = isDefault
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text("Save Arrow Set")
            }
        }
    }
}