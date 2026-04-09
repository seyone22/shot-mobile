package dev.seyone.shot.ui.screen.more.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.seyone.core.domain.AgeGroup
import dev.seyone.core.domain.Gender
import dev.seyone.shot.ui.screen.session.components.DropdownField

@Composable
fun AddArcherDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, club: String, gender: Gender, age: AgeGroup) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var club by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var ageGroup by remember { mutableStateOf(AgeGroup.SENIOR) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Archer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                OutlinedTextField(value = club, onValueChange = { club = it }, label = { Text("Club (Optional)") })

                Text("Gender", style = MaterialTheme.typography.labelMedium)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    Gender.entries.forEachIndexed { index, g ->
                        SegmentedButton(
                            selected = gender == g,
                            onClick = { gender = g },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)
                        ) { Text(g.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }

                DropdownField( // Reusing your helper from the Session Dialog
                    label = "Age Group",
                    options = AgeGroup.entries.map { it.label },
                    selectedOption = ageGroup.label,
                    onOptionSelected = { label ->
                        ageGroup = AgeGroup.entries.find { it.label == label } ?: AgeGroup.SENIOR
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, club, gender, ageGroup) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}