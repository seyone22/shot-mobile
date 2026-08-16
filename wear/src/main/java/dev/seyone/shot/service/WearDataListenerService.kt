package dev.seyone.shot.service

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import dev.seyone.core.data.sync.WearDataSyncManager
import dev.seyone.core.domain.InputMethod
import dev.seyone.core.domain.SessionType
import dev.seyone.core.domain.model.Session

class WearDataListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == WearDataSyncManager.PATH_ACTIVE_SESSION) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val session = Session(
                    id = dataMap.getLong("sessionId"),
                    roundId = dataMap.getLong("roundId"),
                    sessionType = SessionType.valueOf(dataMap.getString("sessionType") ?: "PRACTICE"),
                    inputMethod = InputMethod.valueOf(dataMap.getString("inputMethod") ?: "ARROW_VALUES"),
                    numberOfArchers = dataMap.getInt("numberOfArchers"),
                    arrowsPerEnd = dataMap.getInt("arrowsPerEnd"),
                    notes = dataMap.getString("notes") ?: "",
                    timestamp = dataMap.getLong("timestamp")
                )
                // Post session to local event dispatcher or database repository
                WearSessionBroadcast.broadcastSession(session)
            }
        }
    }
}

object WearSessionBroadcast {
    private var listener: ((Session) -> Unit)? = null

    fun registerListener(onSessionReceived: (Session) -> Unit) {
        listener = onSessionReceived
    }

    fun broadcastSession(session: Session) {
        listener?.invoke(session)
    }
}
