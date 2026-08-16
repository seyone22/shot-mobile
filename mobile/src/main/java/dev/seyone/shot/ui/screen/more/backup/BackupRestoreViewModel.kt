package dev.seyone.shot.ui.screen.more.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.seyone.core.data.repository.BackupRepository
import dev.seyone.core.data.repository.BackupSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RestoreMode {
    MERGE,
    OVERWRITE
}

data class BackupRestoreUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val summary: BackupSummary = BackupSummary(),
    val restoreMode: RestoreMode = RestoreMode.MERGE,
    val userMessage: String? = null,
    val isError: Boolean = false
)

class BackupRestoreViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupRestoreUiState())
    val uiState: StateFlow<BackupRestoreUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    fun loadSummary() {
        viewModelScope.launch {
            try {
                val summary = backupRepository.getBackupSummary()
                _uiState.update { it.copy(summary = summary) }
            } catch (e: Exception) {
                // Ignore initial summary load error
            }
        }
    }

    fun setRestoreMode(mode: RestoreMode) {
        _uiState.update { it.copy(restoreMode = mode) }
    }

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, userMessage = null) }
            try {
                val exportedSummary = withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                        val summary = backupRepository.exportBackup(outputStream)
                        outputStream.flush()
                        summary
                    } ?: throw IllegalStateException("Could not open destination file for writing")
                }
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        userMessage = "Backup exported successfully! (${exportedSummary.sessionsCount} sessions, ${exportedSummary.bowProfilesCount} bows)",
                        isError = false
                    )
                }
                loadSummary()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        userMessage = "Failed to export backup: ${e.localizedMessage}",
                        isError = true
                    )
                }
            }
        }
    }

    fun importBackup(context: Context, uri: Uri, mergeMode: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, userMessage = null) }
            try {
                val importedSummary = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        backupRepository.importBackup(inputStream, mergeMode)
                    } ?: throw IllegalStateException("Could not open backup file for reading")
                }
                val modeLabel = if (mergeMode) "merged" else "restored"
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        userMessage = "Data successfully $modeLabel! (${importedSummary.sessionsCount} sessions, ${importedSummary.archersCount} archers)",
                        isError = false
                    )
                }
                loadSummary()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        userMessage = "Failed to import backup file: ${e.localizedMessage}",
                        isError = true
                    )
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    class Factory(private val backupRepository: BackupRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupRestoreViewModel(backupRepository) as T
        }
    }
}
