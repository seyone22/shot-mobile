package dev.seyone.shot.ui.screen.statistics

// Vico 2.x Imports
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
import java.text.SimpleDateFormat
import java.util.Calendar
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import dev.seyone.shot.di.AppViewModelProvider
import dev.seyone.shot.ui.theme.ArcheryColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    // Controls visibility of the Custom Date Range Picker
    var showDatePicker by remember { mutableStateOf(false) }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Statistics") },
                scrollBehavior = scrollBehavior
            )
        }
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
                        SummaryCard(
                            "Total Arrows",
                            uiState.totalArrowsShot.toString(),
                            Modifier.weight(1f)
                        )
                        SummaryCard(
                            "Overall Avg",
                            String.format(Locale.getDefault(), "%.2f", uiState.overallAverage),
                            Modifier.weight(1f)
                        )
                    }

                    val goldRate =
                        if (uiState.totalArrowsShot > 0) (uiState.goldsCount.toFloat() / uiState.totalArrowsShot) * 100 else 0f
                    val hitRate =
                        if (uiState.totalArrowsShot > 0) (uiState.hitsCount.toFloat() / uiState.totalArrowsShot) * 100 else 0f

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            "Gold Rate",
                            "${String.format(Locale.getDefault(), "%.1f", goldRate)}%",
                            Modifier.weight(1f)
                        )
                        SummaryCard(
                            "Hit Rate",
                            "${String.format(Locale.getDefault(), "%.1f", hitRate)}%",
                            Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard(
                            "Total Sessions",
                            uiState.totalSessions.toString(),
                            Modifier.weight(1f)
                        )
                        SummaryCard(
                            "Arrows/Session",
                            String.format(
                                Locale.getDefault(),
                                "%.1f",
                                uiState.averageArrowsPerSession
                            ),
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- 3. Score Distribution (Vico Column Chart) ---
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Score Breakdown",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${uiState.totalArrowsShot} arrows total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ScoreDistributionChart(uiState.scoreDistribution, uiState.totalArrowsShot)
                    }
                }
            }

            // --- 4. Daily Arrow Volume Heatmap (GitHub Contribution Style) ---
            item {
                GitHubContributionHeatmap(uiState.dailyStats)
            }

            // --- 5. Daily Average Chart (Vico Line Chart) ---
            if (uiState.dailyStats.size >= 2) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Daily Average Trend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AverageTrendChart(uiState.dailyStats)
                        }
                    }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 8.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ScoreDistributionChart(distribution: Map<String, Int>, totalArrows: Int) {
    if (totalArrows == 0) {
        Text(
            "No data available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val sortedKeys = listOf("M", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "X")
    val counts = sortedKeys.map { key -> distribution[key] ?: 0 }
    val modelProducer = remember { CartesianChartModelProducer() }

    // SPLIT THE DATA (UPDATED FOR REVERSED ORDER)
    val missCounts = counts.mapIndexed { i, c -> if (i == 0) c else 0 }       // M
    val whiteCounts = counts.mapIndexed { i, c -> if (i in 1..2) c else 0 }   // 1, 2
    val blackCounts = counts.mapIndexed { i, c -> if (i in 3..4) c else 0 }   // 3, 4
    val blueCounts = counts.mapIndexed { i, c -> if (i in 5..6) c else 0 }    // 5, 6
    val redCounts = counts.mapIndexed { i, c -> if (i in 7..8) c else 0 }     // 7, 8
    val goldCounts = counts.mapIndexed { i, c -> if (i in 9..11) c else 0 }   // 9, 10, X

    LaunchedEffect(distribution) {
        modelProducer.runTransaction {
            columnSeries {
                series(missCounts)
                series(whiteCounts)
                series(blackCounts)
                series(blueCounts)
                series(redCounts)
                series(goldCounts)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    // 1. Miss (Index 0)
                    rememberLineComponent(
                        fill = Fill(ArcheryColors.Miss),
                        thickness = 12.dp,
                        shape = RectangleShape
                    ),
                    // 2. White (Index 1, 2)
                    rememberLineComponent(
                        fill = Fill(ArcheryColors.White),
                        thickness = 12.dp,
                        shape = RectangleShape,
                        strokeFill = Fill(Color.LightGray),
                        strokeThickness = 1.dp
                    ),
                    // 3. Black (Index 3, 4)
                    rememberLineComponent(
                        fill = Fill(ArcheryColors.Black),
                        thickness = 12.dp,
                        shape = RectangleShape,
                        strokeFill = Fill(Color.LightGray)
                    ),
                    // 4. Blue (Index 5, 6)
                    rememberLineComponent(
                        fill = Fill(ArcheryColors.Blue),
                        thickness = 12.dp,
                        shape = RectangleShape
                    ),
                    // 5. Red (Index 7, 8)
                    rememberLineComponent(
                        fill = Fill(ArcheryColors.Red),
                        thickness = 12.dp,
                        shape = RectangleShape
                    ),
                    // 6. Gold (Index 9, 10, X)
                    rememberLineComponent(
                        fill = Fill(ArcheryColors.Gold),
                        thickness = 12.dp,
                        shape = RectangleShape
                    )
                ),
                // Stack them so they occupy the same horizontal space seamlessly!
                mergeMode = { ColumnCartesianLayer.MergeMode.Stacked }
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = { _, value, _ -> value.toInt().toString() }
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ ->
                    val index = value.toInt()
                    if (index in sortedKeys.indices) sortedKeys[index] else ""
                }
            ),
            marker = rememberMarker() // <-- Added Tooltip!
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
fun GitHubContributionHeatmap(
    dailyStats: List<DailyStat>,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()

    val statsMap = remember(dailyStats) {
        dailyStats.associate { it.dateMs to it.totalArrows }
    }

    val (weeks, monthLabels) = remember(statsMap) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val weekList = mutableListOf<List<HeatmapCellData>>()
        val monthLabelList = mutableListOf<Pair<Int, String>>()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val tempCal = calendar.clone() as Calendar
        tempCal.add(Calendar.DAY_OF_YEAR, -(52 * 7 - 1))

        var lastMonth = -1

        for (weekIdx in 0 until 52) {
            val daysInWeek = mutableListOf<HeatmapCellData>()
            for (dayIdx in 0 until 7) {
                val currentMonth = tempCal.get(Calendar.MONTH)
                if (dayIdx == 0 && currentMonth != lastMonth) {
                    monthLabelList.add(Pair(weekIdx, monthFormat.format(tempCal.time)))
                    lastMonth = currentMonth
                }

                val dayMs = tempCal.timeInMillis
                val count = statsMap[dayMs] ?: 0
                val level = when {
                    count == 0 -> 0
                    count <= 24 -> 1
                    count <= 48 -> 2
                    count <= 96 -> 3
                    else -> 4
                }

                daysInWeek.add(
                    HeatmapCellData(
                        dateMs = dayMs,
                        dateString = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(tempCal.time),
                        arrowCount = count,
                        level = level
                    )
                )

                tempCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            weekList.add(daysInWeek)
        }

        Pair(weekList, monthLabelList)
    }

    var selectedCell by remember { mutableStateOf<HeatmapCellData?>(null) }

    LaunchedEffect(weeks) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Arrow Volume",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                selectedCell?.let { cell ->
                    val label = if (cell.arrowCount == 1) "1 arrow" else "${cell.arrowCount} arrows"
                    Text(
                        text = "$label on ${cell.dateString}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } ?: run {
                    val totalArrows = dailyStats.sumOf { it.totalArrows }
                    Text(
                        text = "$totalArrows arrows total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(top = 22.dp, end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val dayLabels = listOf("", "Mon", "", "Wed", "", "Fri", "")
                    dayLabels.forEach { dayLabel ->
                        Box(
                            modifier = Modifier.size(height = 12.dp, width = 24.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (dayLabel.isNotEmpty()) {
                                Text(
                                    text = dayLabel,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .height(20.dp)
                                .padding(bottom = 2.dp)
                        ) {
                            var currentColumn = 0
                            for (monthPair in monthLabels) {
                                val (weekIndex, monthName) = monthPair
                                val spacerWidth = ((weekIndex - currentColumn) * 15).dp
                                if (spacerWidth > 0.dp) {
                                    Spacer(modifier = Modifier.width(spacerWidth))
                                }
                                Text(
                                    text = monthName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(45.dp)
                                )
                                currentColumn = weekIndex + 3
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (week in weeks) {
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    for (cell in week) {
                                        val cellColor = getHeatmapColor(cell.level, isDark)
                                        val isSelected = selectedCell == cell

                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(cellColor)
                                                .then(
                                                    if (isSelected) Modifier.border(
                                                        1.5.dp,
                                                        MaterialTheme.colorScheme.primary,
                                                        RoundedCornerShape(3.dp)
                                                    ) else Modifier
                                                )
                                                .clickable { selectedCell = cell }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily activity heatmap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Less",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    for (lvl in 0..4) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(getHeatmapColor(lvl, isDark))
                        )
                    }
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class HeatmapCellData(
    val dateMs: Long,
    val dateString: String,
    val arrowCount: Int,
    val level: Int
)

private fun getHeatmapColor(level: Int, isDark: Boolean): Color {
    return if (isDark) {
        when (level) {
            0 -> Color(0xFF161B22)
            1 -> Color(0xFF0E4429)
            2 -> Color(0xFF006D32)
            3 -> Color(0xFF26A641)
            else -> Color(0xFF39D353)
        }
    } else {
        when (level) {
            0 -> Color(0xFFEBEDF0)
            1 -> Color(0xFF9BE9A8)
            2 -> Color(0xFF40C463)
            3 -> Color(0xFF30A14E)
            else -> Color(0xFF216E39)
        }
    }
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
            ),
            marker = rememberMarker() // <-- Added Tooltip!
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

/**
 * Standard Vico Tooltip Marker Implementation
 * Displays a nice floating pill over the bar/line when tapped.
 */
@Composable
fun rememberMarker(): CartesianMarker {
    val labelBackground = rememberShapeComponent(
        fill = Fill(MaterialTheme.colorScheme.surfaceContainerHigh),
        strokeFill = Fill(MaterialTheme.colorScheme.outlineVariant),
        strokeThickness = 1.dp
    )

    val label = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
        background = labelBackground,
        padding = Insets(8.dp, 4.dp),
    )

    val indicator = rememberShapeComponent(
        fill = Fill(MaterialTheme.colorScheme.primary),
    )

    val guideline = rememberLineComponent(
        fill = Fill(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        thickness = 1.dp
    )

    // THE FIX: Use rememberDefaultCartesianMarker
    return rememberDefaultCartesianMarker(
        label = label,
        // Vico passes a color parameter here based on the line/column color.
        // We use `_ ->` to ignore it and stick to our Primary theme color.
        indicator = { _ -> indicator },
        guideline = guideline
    )
}