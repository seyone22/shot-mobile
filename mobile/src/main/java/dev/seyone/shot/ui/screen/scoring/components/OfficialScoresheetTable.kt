package dev.seyone.shot.ui.screen.scoring.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.seyone.shot.ui.screen.scoring.ArrowInput
import dev.seyone.shot.ui.screen.scoring.ScoringUiState
import dev.seyone.shot.ui.theme.ArcheryColors
import java.util.Locale

/**
 * Data representation for a single row in the Official Scoresheet Table.
 */
data class ScoresheetTableRow(
    val endNumberLabel: String,      // e.g., "1a", "1b", "2a", "2b" or "1", "2"
    val isSecondPass: Boolean,       // True if this is pass B of a 6-arrow end
    val arrows: List<String>,        // 3 arrow string values (sorted descending for WA rules)
    val passTotal: Int?,             // 3-arrow pass subtotal
    val endTotal: Int?,              // 6-arrow end total (shown on pass B)
    val runningSum: Int?,            // Cumulative running sum (shown on pass B or end row)
    val isCompleteEnd: Boolean
)

/**
 * Helper to parse and sort arrow inputs in descending order per World Archery competition rules.
 */
private fun sortArrowsDescending(arrows: List<ArrowInput>): List<String> {
    return arrows.map { it.value }.sortedWith(Comparator { a, b ->
        val rankA = getArrowRank(a)
        val rankB = getArrowRank(b)
        rankB.compareTo(rankA) // Higher rank comes first
    })
}

private fun getArrowRank(value: String): Int {
    return when (value) {
        "X" -> 11
        "10" -> 10
        "9" -> 9
        "8" -> 8
        "7" -> 7
        "6" -> 6
        "5" -> 5
        "4" -> 4
        "3" -> 3
        "2" -> 2
        "1" -> 1
        "M" -> 0
        else -> -1
    }
}

@Composable
fun OfficialScoresheetTable(
    state: ScoringUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Row 1: Session / Event Name or Archer Name / Round Title + Date
                    val headerTitle = remember(state.sessionName, state.archerName, state.title) {
                        when {
                            state.sessionName.isNotBlank() -> state.sessionName
                            state.archerName.isNotBlank() -> state.archerName
                            else -> state.title
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (state.sessionDateText.isNotBlank()) {
                            Text(
                                text = state.sessionDateText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    if (state.sessionName.isNotBlank() && state.archerName.isNotBlank()) {
                        Text(
                            text = "Archer: ${state.archerName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Row 2: Equipment & Location + Distance & Target Face Specs
                    val equipAndLocation = remember(state.bowProfileName, state.locationName) {
                        listOfNotNull(
                            state.bowProfileName.ifBlank { null },
                            state.locationName.ifBlank { null }
                        ).joinToString(" • ")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (equipAndLocation.isNotBlank()) {
                            Text(
                                text = equipAndLocation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${state.activeDistanceText} (${state.activeTargetFace.description})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- MAIN SCORESHEET TABLE CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Column Headers
                    ScoresheetTableHeader()
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Process Ends and build rows
                    val rows = generateScoresheetRows(state.ends, state.activeArrowsPerEnd)

                    rows.forEachIndexed { index, row ->
                        ScoresheetTableRowItem(
                            row = row,
                            isEven = index % 2 == 0
                        )
                        if (index < rows.lastIndex) {
                            HorizontalDivider(
                                color = if (row.isSecondPass) MaterialTheme.colorScheme.outlineVariant 
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                thickness = if (row.isSecondPass) 1.5.dp else 0.5.dp
                            )
                        }
                    }
                }
            }
        }

        // --- OFFICIAL FOOTER SUMMARY CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Round Summary & Tie-Breakers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatPill(label = "Total Score", value = "${state.totalScore}", isHighlight = true)
                        StatPill(label = "10's + X's", value = "${state.tenPlusXCount}")
                        StatPill(label = "X's Only", value = "${state.xCount}")
                        StatPill(
                            label = "Average", 
                            value = String.format(Locale.getDefault(), "%.2f", state.average)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Digital Verification Signature Mock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Scorer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "_________________",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Column {
                            Text(
                                text = "Archer Signature",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "_________________",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoresheetTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("End", modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Text("1", modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Text("2", modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Text("3", modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Text("Pass", modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text("End", modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        Text("Sum", modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun ScoresheetTableRowItem(
    row: ScoresheetTableRow,
    isEven: Boolean
) {
    val bgColor = if (isEven) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // End Number Label (e.g. "1a", "1b", "2a", "2b")
        Text(
            text = row.endNumberLabel,
            modifier = Modifier.weight(0.9f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (row.isSecondPass) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )

        // 3 Arrow slots (sorted high to low)
        for (i in 0..2) {
            val arrowVal = row.arrows.getOrNull(i) ?: ""
            Box(
                modifier = Modifier.weight(0.8f),
                contentAlignment = Alignment.Center
            ) {
                if (arrowVal.isNotEmpty()) {
                    ArrowPill(value = arrowVal)
                } else {
                    Text("-", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        // Pass 3-arrow Total
        Text(
            text = row.passTotal?.toString() ?: "-",
            modifier = Modifier.weight(1.0f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        // 6-arrow End Total
        Text(
            text = row.endTotal?.toString() ?: "-",
            modifier = Modifier.weight(1.0f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Running Cumulative Sum
        Text(
            text = row.runningSum?.toString() ?: "-",
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun ArrowPill(value: String) {
    val (bgColor, textColor) = when (value) {
        "X", "10", "9" -> ArcheryColors.Gold to Color.Black
        "8", "7" -> ArcheryColors.Red to Color.White
        "6", "5" -> ArcheryColors.Blue to Color.White
        "4", "3" -> Color(0xFF333333) to Color.White
        "2", "1" -> Color.White to Color.Black
        "M" -> Color(0xFF4CAF50) to Color.White
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = if (value == "2" || value == "1") BorderStroke(1.dp, Color.LightGray) else null
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Transforms raw ends into official competition table rows (handling 3+3 passes for 6-arrow ends).
 */
private fun generateScoresheetRows(
    ends: List<List<ArrowInput>>,
    arrowsPerEnd: Int
): List<ScoresheetTableRow> {
    val rows = mutableListOf<ScoresheetTableRow>()
    var runningSum = 0

    ends.forEachIndexed { endIdx, rawEndArrows ->
        val endNum = endIdx + 1

        if (arrowsPerEnd > 3) {
            // Split 6-arrow end into Pass A (1..3) and Pass B (4..6)
            val passAArrows = rawEndArrows.take(3)
            val passBArrows = rawEndArrows.drop(3).take(3)

            val sortedPassA = sortArrowsDescending(passAArrows)
            val sortedPassB = sortArrowsDescending(passBArrows)

            val passATotal = if (passAArrows.isNotEmpty()) passAArrows.sumOf { it.score } else null
            val passBTotal = if (passBArrows.isNotEmpty()) passBArrows.sumOf { it.score } else null

            val endTotal = if (passATotal != null || passBTotal != null) {
                (passATotal ?: 0) + (passBTotal ?: 0)
            } else null

            if (endTotal != null) {
                runningSum += endTotal
            }

            // Row 1a (Pass A)
            rows.add(
                ScoresheetTableRow(
                    endNumberLabel = "${endNum}a",
                    isSecondPass = false,
                    arrows = sortedPassA,
                    passTotal = passATotal,
                    endTotal = null,
                    runningSum = null,
                    isCompleteEnd = false
                )
            )

            // Row 1b (Pass B)
            rows.add(
                ScoresheetTableRow(
                    endNumberLabel = "${endNum}b",
                    isSecondPass = true,
                    arrows = sortedPassB,
                    passTotal = passBTotal,
                    endTotal = endTotal,
                    runningSum = if (endTotal != null) runningSum else null,
                    isCompleteEnd = rawEndArrows.size >= arrowsPerEnd
                )
            )
        } else {
            // Standard 3-arrow end (Indoor)
            val sortedPass = sortArrowsDescending(rawEndArrows)
            val endTotal = if (rawEndArrows.isNotEmpty()) rawEndArrows.sumOf { it.score } else null

            if (endTotal != null) {
                runningSum += endTotal
            }

            rows.add(
                ScoresheetTableRow(
                    endNumberLabel = "$endNum",
                    isSecondPass = true,
                    arrows = sortedPass,
                    passTotal = endTotal,
                    endTotal = endTotal,
                    runningSum = if (endTotal != null) runningSum else null,
                    isCompleteEnd = rawEndArrows.size >= arrowsPerEnd
                )
            )
        }
    }

    return rows
}
