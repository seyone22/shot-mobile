package dev.seyone.core.data.sync

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dev.seyone.core.domain.model.Arrow
import dev.seyone.core.domain.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class WearDataSyncManager(private val context: Context) {
    private val dataClient: DataClient by lazy { Wearable.getDataClient(context) }
    private val messageClient: MessageClient by lazy { Wearable.getMessageClient(context) }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _syncedSession = MutableStateFlow<Session?>(null)
    val syncedSession: StateFlow<Session?> = _syncedSession.asStateFlow()

    fun pushActiveSessionToWear(session: Session) {
        scope.launch {
            try {
                val request = PutDataMapRequest.create(PATH_ACTIVE_SESSION).apply {
                    dataMap.putLong("sessionId", session.id)
                    dataMap.putLong("roundId", session.roundId)
                    dataMap.putString("sessionType", session.sessionType.name)
                    dataMap.putString("inputMethod", session.inputMethod.name)
                    dataMap.putInt("numberOfArchers", session.numberOfArchers)
                    dataMap.putInt("arrowsPerEnd", session.arrowsPerEnd)
                    dataMap.putString("notes", session.notes)
                    dataMap.putLong("timestamp", session.timestamp)
                }.asPutDataRequest().setUrgent()

                dataClient.putDataItem(request).await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendArrowShotToPhone(score: Int, isX: Boolean, x: Float? = null, y: Float? = null) {
        scope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                val payload = "$score,$isX,${x ?: ""},${y ?: ""}".toByteArray()

                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, PATH_ARROW_SHOT, payload).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendUndoActionToPhone() {
        scope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, PATH_UNDO_SHOT, ByteArray(0)).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendCompleteEndToPhone() {
        scope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, PATH_COMPLETE_END, ByteArray(0)).await()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        const val PATH_ACTIVE_SESSION = "/active_session"
        const val PATH_ARROW_SHOT = "/arrow_shot"
        const val PATH_UNDO_SHOT = "/undo_shot"
        const val PATH_COMPLETE_END = "/complete_end"
    }
}
