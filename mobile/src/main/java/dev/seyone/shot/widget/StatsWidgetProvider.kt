package dev.seyone.shot.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.seyone22.shot.R
import dev.seyone.core.data.ShotDatabase
import dev.seyone.shot.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class StatsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_stats)

            // Intent to launch MainActivity into Statistics screen
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "statistics")
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.widget_stats_container, pendingIntent)

            // Fetch live Room database metrics on IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = ShotDatabase.getDatabase(context)
                    val sessions = db.sessionDao().getAllSessionsSync()
                    val totalSessions = sessions.size

                    var totalArrows = 0
                    var totalScoreSum = 0
                    var todayArrows = 0

                    val todayCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val todayMs = todayCal.timeInMillis

                    for (s in sessions) {
                        val ends = db.scoringDao().getEndsWithArrowsForSessionSync(s.id)
                        for (end in ends) {
                            val arrowCount = end.arrows.size
                            totalArrows += arrowCount
                            totalScoreSum += end.arrows.sumOf { it.scoreValue }
                            if (s.timestamp >= todayMs) {
                                todayArrows += arrowCount
                            }
                        }
                    }

                    val overallAvg = if (totalArrows > 0) totalScoreSum.toFloat() / totalArrows else 0f

                    views.setTextViewText(R.id.widget_today_volume, "$todayArrows Today")
                    views.setTextViewText(R.id.widget_stat_avg, String.format(Locale.getDefault(), "%.2f", overallAvg))
                    views.setTextViewText(R.id.widget_stat_sessions, totalSessions.toString())

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                } catch (e: Exception) {
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }
}
