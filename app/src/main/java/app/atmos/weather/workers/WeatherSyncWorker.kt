package app.atmos.weather.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.atmos.weather.AtmosApplication
import app.atmos.weather.MainActivity
import app.atmos.weather.R
import app.atmos.weather.data.model.BackgroundRefreshInterval
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class WeatherSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "atmos_periodic_weather_sync"
        const val CHANNEL_ALERTS = "atmos_weather_alerts"
        const val CHANNEL_SUMMARY = "atmos_daily_summary"

        fun schedule(context: Context, interval: BackgroundRefreshInterval) {
            val workManager = WorkManager.getInstance(context)
            if (interval == BackgroundRefreshInterval.OFF) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // WorkManager minimum periodic interval is 15 minutes
            val minutes = interval.intervalMinutes.coerceAtLeast(15)

            val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
                minutes, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        val app = context.applicationContext as? AtmosApplication ?: return Result.failure()
        val repo = app.weatherRepository
        val prefs = repo.userPreferencesFlow.first()

        try {
            val location = repo.getSelectedLocation()
            val weatherResult = repo.fetchWeather(location, forceRefresh = true)

            if (weatherResult.isSuccess) {
                val overview = weatherResult.getOrThrow()

                createNotificationChannels()

                // Check and dispatch severe weather alert notifications
                if (prefs.weatherAlertNotifications && overview.alerts.isNotEmpty()) {
                    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true

                    if (hasNotificationPermission) {
                        overview.alerts.forEach { alert ->
                            sendAlertNotification(alert.title, alert.description, alert.id.hashCode())
                        }
                    }
                }
                return Result.success()
            }
            return Result.retry()
        } catch (_: Exception) {
            return Result.retry()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Severe Weather Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts for severe storms, extreme heat, cold, and high winds"
                enableVibration(true)
            }

            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARY,
                "Daily Weather Summary",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Daily forecast condition updates"
            }

            manager.createNotificationChannel(alertChannel)
            manager.createNotificationChannel(summaryChannel)
        }
    }

    private fun sendAlertNotification(title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.atmos_app_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {}
    }
}
