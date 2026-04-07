package dev.seyone.shot.ui.screen.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.seyone.shot.di.AppViewModelProvider
import java.util.Locale

// Vico 2.x Imports
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Controls visibility of the Custom Date Range Picker
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Statistics") }) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. Filters ---
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TimeRange.entries) { range ->
                        FilterChip(
                            selected = uiState.selectedTimeRange == range,
                            onClick = {
                                if (range == TimeRange.CUSTOM) {
                                    showDatePicker = true
                                } else {
                                    viewModel.setTimeRange(range)
                                }
                            },
                            label = { Text(range.displayName) }
                        )
                    }
                }
            }

            // --- 2. Advanced Summaries Grid ---
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Total Arrows", uiState.totalArrowsShot.toString(), Modifier.weight(1f))
                        SummaryCard("Overall Avg", String.format(Locale.getDefault(), "%.2f", uiState.overallAverage), Modifier.weight(1f))
                    }

                    val goldRate = if(uiState.totalArrowsShot > 0) (uiState.goldsCount.toFloat() / uiState.totalArrowsShot) * 100 else 0f
                    val hitRate = if(uiState.totalArrowsShot > 0) (uiState.hitsCount.toFloat() / uiState.totalArrowsShot) * 100 else 0f

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Gold Rate", "${String.format(Locale.getDefault(), "%.1f", goldRate)}%", Modifier.weight(1f))
                        SummaryCard("Hit Rate", "${String.format(Locale.getDefault(), "%.1f", hitRate)}%", Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Total Sessions", uiState.totalSessions.toString(), Modifier.weight(1f))
                        SummaryCard("Arrows/Session", String.format(Locale.getDefault(), "%.1f", uiState.averageArrowsPerSession), Modifier.weight(1f))
                    }
                }
            }

            // --- 3. Score Distribution (Vico Column Chart) ---
            item {
                Text("Score Breakdown", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                ScoreDistributionChart(uiState.scoreDistribution, uiState.totalArrowsShot)
            }

            // --- 4. Daily Arrow Volume Chart (Vico Column Chart) ---
            if (uiState.dailyStats.isNotEmpty()) {
                item {
                    Text("Daily Arrow Volume", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    VolumeTrendChart(uiState.dailyStats)
                }
            }

            // --- 5. Daily Average Chart (Vico Line Chart) ---
            if (uiState.dailyStats.size >= 2) {
                item {
                    Text("Daily Average Trend", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    AverageTrendChart(uiState.dailyStats)
                }
            }
        }
    }

    // --- Custom Date Range Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState()

        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Select Date Range", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { showDatePicker = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    DateRangePicker(
                        state = datePickerState,
                        modifier = Modifier.weight(1f),
                        title = null,
                        headline = null,
                        showModeToggle = false
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val start = datePickerState.selectedStartDateMillis
                            val end = datePickerState.selectedEndDateMillis
                            if (start != null && end != null) {
                                viewModel.setCustomDateRange(start, end)
                            }
                            showDatePicker = false
                        }) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

// --- Components ---

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ScoreDistributionChart(distribution: Map<String, Int>, totalArrows: Int) {
    if (totalArrows == 0) {
        Text("No data available", style = MaterialTheme.typography.bodyMedium)
        return
    }

    val sortedKeys = listOf("X", "10", "9", "8", "7", "6", "5", "4", "3", "2", "1", "M")
    val counts = sortedKeys.map { key -> distribution[key] ?: 0 }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(distribution) {
        modelProducer.runTransaction { columnSeries { series(counts) } }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(valueFormatter = { _, value, _ -> value.toInt().toString() }),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ ->
                    val index = value.toInt()
                    if (index in sortedKeys.indices) sortedKeys[index] else ""
                }
            ),
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Composable
fun VolumeTrendChart(dailyStats: List<DailyStat>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyStats) {
        modelProducer.runTransaction {
            columnSeries { series(dailyStats.map { it.totalArrows }) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(valueFormatter = { _, value, _ -> value.toInt().toString() }),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ -> "Day ${value.toInt() + 1}" }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}

@Composable
fun AverageTrendChart(dailyStats: List<DailyStat>) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dailyStats) {
        modelProducer.runTransaction {
            lineSeries { series(dailyStats.map { it.averageScore }) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ -> "Day ${value.toInt() + 1}" }
            )
        ),
        modelProducer = modelProducer,
        modifier = Modifier.fillMaxWidth().height(200.dp)
    )
}