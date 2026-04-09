package dev.seyone.shot.presentation.screen.shotscreen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.model.Session

@Composable
fun SessionListScreen(onSessionSelected: (Session) -> Unit) {
    val columnState = rememberScalingLazyListState(
    )

    // Mocking domain sessions based on your SRS
    val mockNumericSession = Session(
        id = 1,
        roundId = 20,
        sessionType = SessionType.PRACTICE,
        inputMethod = InputMethod.ARROW_VALUES,
        numberOfArchers = 1,
        arrowsPerEnd = 3,
        timestamp = System.currentTimeMillis()
    )
    val mockTargetSession = Session(
        id = 2,
        roundId = 3,
        sessionType = SessionType.PRACTICE,
        inputMethod = InputMethod.TARGET_FACE,
        numberOfArchers = 1,
        arrowsPerEnd = 6,
        timestamp = System.currentTimeMillis()
    )

    ScreenScaffold(scrollState = columnState) {
        ScalingLazyColumn(state = columnState) {
            item { Text(text = "Recent Sessions") }

            item {
                TitleCard(
                    onClick = { onSessionSelected(mockNumericSession) },
                    title = { Text("WA 18m Indoor") },
                    time = { Text("Just now") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Numeric • 3 Arrows/End")
                }
            }

            item {
                TitleCard(
                    onClick = { onSessionSelected(mockTargetSession) },
                    title = { Text("WA 70m Recurve") },
                    time = { Text("Yesterday") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Target Face • 6 Arrows/End")
                }
            }
        }
    }
}