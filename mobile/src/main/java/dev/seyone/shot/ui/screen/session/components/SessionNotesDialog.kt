package dev.seyone.shot.ui.screen.session.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SessionNotesDialog(
    initialNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember { mutableStateOf(initialNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Session Notes") },
        text = {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                placeholder = { Text("Weather, equipment tweaks, how you felt...") },
                maxLines = 6
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(notes) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}