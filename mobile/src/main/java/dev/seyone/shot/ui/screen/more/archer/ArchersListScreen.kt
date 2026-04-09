package dev.seyone.shot.ui.screen.more.archer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.seyone.core.domain.model.Archer
import dev.seyone.core.domain.repository.ArcherRepository
import dev.seyone.shot.ui.screen.more.MoreViewModel
import dev.seyone.shot.ui.screen.more.components.AddArcherDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchersListScreen(
    onNavigateBack: () -> Unit,
    archerRepository: ArcherRepository // Pass this from MainActivity
) {
    val viewModel: MoreViewModel = viewModel(
        factory = MoreViewModel.Factory(archerRepository)
    )

    val archers by viewModel.archers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = { Text("Archers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        if (archers.isEmpty()) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No archers added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(archers, key = { it.id }) { archer ->
                    ArcherCard(
                        archer = archer,
                        onDelete = { viewModel.deleteArcher(archer) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddArcherDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { n, c, g, a ->
                    viewModel.addArcher(n, c, g, a)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ArcherCard(archer: Archer, onDelete: () -> Unit) {
    ElevatedCard {
        ListItem(
            headlineContent = { Text(archer.name) },
            supportingContent = {
                Text("${archer.ageGroup.label} • ${archer.gender.name.lowercase()} ${archer.clubName?.let { "• $it" } ?: ""}")
            },
            trailingContent = {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}