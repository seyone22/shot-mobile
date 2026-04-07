package dev.seyone.shot.ui.screen.more.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.seyone.shot.data.local.entity.BowComponentEntity
import dev.seyone.shot.data.local.entity.ComponentCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentBottomSheet(
    bowId: Long,
    initialComponent: BowComponentEntity?,
    onDismiss: () -> Unit,
    onSave: (BowComponentEntity) -> Unit
) {
    // Component State
    var category by remember {
        mutableStateOf(
            initialComponent?.category ?: ComponentCategory.RISER
        )
    }
    var brand by remember { mutableStateOf(initialComponent?.brand ?: "") }
    var model by remember { mutableStateOf(initialComponent?.model ?: "") }
    var price by remember { mutableStateOf(initialComponent?.price?.toString() ?: "") }
    var notes by remember { mutableStateOf(initialComponent?.notes ?: "") }

    // Dropdown State
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
                text = if (initialComponent == null) "Add Component" else "Edit Component",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = category.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded, onDismissRequest = { expanded = false }) {
                    ComponentCategory.entries.forEach { cat ->
                        DropdownMenuItem(text = {
                            Text(
                                cat.name.lowercase().replaceFirstChar { it.uppercase() })
                        }, onClick = {
                            category = cat
                            expanded = false
                        })
                    }
                }
            }

            // Brand & Model Row
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand (e.g. Hoyt)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model (e.g. Formula)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes / Details
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (e.g. 70\", 38lbs, Blue)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSave(
                        BowComponentEntity(
                            id = initialComponent?.id ?: 0L,
                            bowProfileId = bowId, // Link it back to the parent bow!
                            category = category,
                            brand = brand,
                            model = model,
                            price = price.toDoubleOrNull(),
                            notes = notes
                        )
                    )
                }, modifier = Modifier.fillMaxWidth(),
                // Require at least a model name before allowing a save
                enabled = model.isNotBlank() || brand.isNotBlank()
            ) {
                Text("Save Component")
            }
        }
    }
}