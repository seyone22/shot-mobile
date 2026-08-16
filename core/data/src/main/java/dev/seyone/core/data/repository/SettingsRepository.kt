package dev.seyone.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light Theme"),
    DARK("Dark Theme")
}

enum class ArrowSortOrder(val displayName: String, val description: String) {
    AS_ENTERED("As Entered", "Display arrows in the order they were shot"),
    DESCENDING("Descending Score", "Display highest scoring arrows first (e.g. X, 10, 9, 8...)")
}

data class UserSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val arrowSortOrder: ArrowSortOrder = ArrowSortOrder.AS_ENTERED,
    val defaultRoundId: Long = 1L
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("shot_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = try { AppThemeMode.valueOf(themeStr) } catch (e: Exception) { AppThemeMode.SYSTEM }

        val sortStr = prefs.getString(KEY_ARROW_SORT_ORDER, ArrowSortOrder.AS_ENTERED.name) ?: ArrowSortOrder.AS_ENTERED.name
        val sortOrder = try { ArrowSortOrder.valueOf(sortStr) } catch (e: Exception) { ArrowSortOrder.AS_ENTERED }

        val defaultRoundId = prefs.getLong(KEY_DEFAULT_ROUND_ID, 1L)

        return UserSettings(themeMode = themeMode, arrowSortOrder = sortOrder, defaultRoundId = defaultRoundId)
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settings.value = loadSettings()
    }

    fun setArrowSortOrder(order: ArrowSortOrder) {
        prefs.edit().putString(KEY_ARROW_SORT_ORDER, order.name).apply()
        _settings.value = loadSettings()
    }

    fun setDefaultRoundId(roundId: Long) {
        prefs.edit().putLong(KEY_DEFAULT_ROUND_ID, roundId).apply()
        _settings.value = loadSettings()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ARROW_SORT_ORDER = "key_arrow_sort_order"
        private const val KEY_DEFAULT_ROUND_ID = "key_default_round_id"
    }
}
