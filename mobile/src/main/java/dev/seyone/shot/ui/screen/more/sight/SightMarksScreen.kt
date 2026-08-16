package dev.seyone.shot.ui.screen.more.sight

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import dev.seyone.core.domain.DistanceUnit
import dev.seyone.core.domain.model.SightMark
import dev.seyone.shot.di.AppViewModelProvider
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SightMarksScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SightMarksViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val bows by viewModel.bows.collectAsState()
    val arrowSets by viewModel.arrowSets.collectAsState()
    val selectedBowId by viewModel.selectedBowId.collectAsState()
    val selectedArrowId by viewModel.selectedArrowId.collectAsState()
    val sightMarks by viewModel.sightMarks.collectAsState()
    val rangeCard by viewModel.rangeCard.collectAsState()
    val customDistanceInput by viewModel.customDistanceInput.collectAsState()
    val customPredictedMark by viewModel.customPredictedMark.collectAsState()

    val activeBow = bows.find { it.id == selectedBowId }
    val activeArrow = arrowSets.find { it.id == selectedArrowId }

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Sight Marks") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedBowId != null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Sight Mark")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. BOW & ARROW FILTER CHIPS ---
            Text(
                text = "Active Bow & Equipment Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(bows) { bow ->
                    FilterChip(
                        selected = bow.id == selectedBowId,
                        onClick = { viewModel.selectBow(bow.id) },
                        label = {
                            val lbsText = bow.drawWeight?.let { " (${it} lbs)" } ?: ""
                            Text("${bow.name}$lbsText")
                        }
                    )
                }
            }

            if (arrowSets.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedArrowId == null,
                            onClick = { viewModel.selectArrow(null) },
                            label = { Text("All Arrow Sets") }
                        )
                    }
                    items(arrowSets) { arrow ->
                        FilterChip(
                            selected = arrow.id == selectedArrowId,
                            onClick = { viewModel.selectArrow(arrow.id) },
                            label = { Text(arrow.name) }
                        )
                    }
                }
            }

            // Active Specs Banner Card
            activeBow?.let { bow ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = bow.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            val poundageStr = bow.drawWeight?.let { "${it} lbs Draw Weight" } ?: "Poundage not specified"
                            val arrowStr = activeArrow?.let { " • ${it.name}" } ?: ""
                            Text(
                                text = "$poundageStr$arrowStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // --- 2. TRAJECTORY BALLISTIC VICO CHART ---
            if (sightMarks.size >= 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Ballistic Trajectory Curve",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        val modelProducer = remember { CartesianChartModelProducer() }
                        LaunchedEffect(sightMarks) {
                            val yVals = sightMarks.map { it.elevationMark }
                            if (yVals.size >= 2) {
                                modelProducer.runTransaction {
                                    lineSeries {
                                        series(yVals)
                                    }
                                }
                            }
                        }

                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom(
                                    valueFormatter = { _, value, _ ->
                                        val idx = value.toInt()
                                        if (idx in sightMarks.indices) {
                                            "${sightMarks[idx].distanceValue.toInt()}m"
                                        } else ""
                                    }
                                )
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }

            // --- 3. CUSTOM DISTANCE PREDICTOR CALCULATOR ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = "Polynomial Distance Estimator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = customDistanceInput,
                            onValueChange = { viewModel.setCustomDistanceInput(it) },
                            label = { Text("Target Distance (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Predicted Sight",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                val predText = customPredictedMark?.let { String.format(Locale.getDefault(), "%.2f", it) } ?: "--"
                                Text(
                                    text = predText,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. MEASURED SIGHT MARKS LIST ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SportsScore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Measured Sight Marks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    if (sightMarks.isEmpty()) {
                        Text(
                            text = "No sight marks logged for this bow profile yet. Tap the + button below to add your first sight mark.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            sightMarks.forEach { mark ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "${mark.distanceValue.toInt()} ${mark.distanceUnit.name.take(1).lowercase()}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (mark.notes.isNotBlank()) {
                                            Text(
                                                text = mark.notes,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.2f", mark.elevationMark),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            mark.windageMark?.let { wind ->
                                                val windStr = if (wind >= 0) "+${wind}" else "$wind"
                                                Text(
                                                    text = "W: $windStr",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }

                                        IconButton(onClick = { viewModel.deleteSightMark(mark) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }

            // --- 5. OUTDOOR RANGE LOOKUP CARD ---
            if (rangeCard.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GridOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Outdoor Range Reference Card",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            rangeCard.forEach { mark ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${mark.distanceValue.toInt()} m",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (mark.isCalculated) {
                                            SuggestionChip(
                                                onClick = { },
                                                label = { Text("Predicted", style = MaterialTheme.typography.labelSmall) }
                                            )
                                        } else {
                                            FilterChip(
                                                selected = true,
                                                onClick = { },
                                                label = { Text("Measured", style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }

                                        Text(
                                            text = String.format(Locale.getDefault(), "%.2f", mark.elevationMark),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (mark.isCalculated) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSightMarkDialog(
            onDismiss = { showAddDialog = false },
            onSave = { distance, unit, elevation, windage, notes ->
                showAddDialog = false
                viewModel.saveSightMark(distance, unit, elevation, windage, notes)
            }
        )
    }
}

@Composable
private fun AddSightMarkDialog(
    onDismiss: () -> Unit,
    onSave: (Float, DistanceUnit, Float, Float?, String) -> Unit
) {
    var distanceText by remember { mutableStateOf("") }
    var elevationText by remember { mutableStateOf("") }
    var windageText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf(DistanceUnit.METERS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Sight Mark") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { distanceText = it },
                        label = { Text("Distance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    FilterChip(
                        selected = unit == DistanceUnit.METERS,
                        onClick = { unit = DistanceUnit.METERS },
                        label = { Text("m") }
                    )
                    FilterChip(
                        selected = unit == DistanceUnit.YARDS,
                        onClick = { unit = DistanceUnit.YARDS },
                        label = { Text("yd") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = elevationText,
                        onValueChange = { elevationText = it },
                        label = { Text("Elevation Mark") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = windageText,
                        onValueChange = { windageText = it },
                        label = { Text("Windage (Opt)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dist = distanceText.toFloatOrNull() ?: return@Button
                    val elev = elevationText.toFloatOrNull() ?: return@Button
                    val wind = windageText.toFloatOrNull()
                    onSave(dist, unit, elev, wind, notesText)
                }
            ) {
                Text("Save Sight Mark")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
