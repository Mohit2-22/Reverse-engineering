package com.fittrack.app.data.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.fittrack.app.MainActivity
import com.fittrack.app.R
import com.fittrack.app.domain.model.ActivityType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Foreground service that keeps GPS recording alive when the app is backgrounded
 * or the screen is off. Required for a fitness app — this is why Strava declares
 * FOREGROUND_SERVICE_LOCATION. It shows a persistent notification with live stats.
 */
@AndroidEntryPoint
class RecordingService : Service() {

    @Inject lateinit var recordingManager: RecordingManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val type = intent.getStringExtra(EXTRA_TYPE)
                    ?.let { runCatching { ActivityType.valueOf(it) }.getOrNull() }
                    ?: ActivityType.RUN
                startForeground(NOTIF_ID, buildNotification("Starting…"))
                recordingManager.start(type)
                observeStats()
            }
            ACTION_PAUSE  -> recordingManager.pause()
            ACTION_RESUME -> recordingManager.resume()
            ACTION_STOP   -> {
                recordingManager.stop()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun observeStats() {
        recordingManager.stats
            .onEach { s ->
                val km = (s.distanceMeters / 1000.0)
                val mins = (s.elapsedTimeMs / 60000)
                val secs = (s.elapsedTimeMs / 1000) % 60
                val text = "%.2f km · %02d:%02d".format(km, mins, secs)
                notificationManager().notify(NOTIF_ID, buildNotification(text))
            }
            .launchIn(scope)
    }

    private fun buildNotification(content: String): Notification {
        ensureChannel()
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FitTrack — recording")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Activity recording",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { setShowBadge(false) }
            notificationManager().createNotificationChannel(ch)
        }
    }

    private fun notificationManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "fittrack.START"
        const val ACTION_PAUSE = "fittrack.PAUSE"
        const val ACTION_RESUME = "fittrack.RESUME"
        const val ACTION_STOP = "fittrack.STOP"
        const val EXTRA_TYPE = "type"
        private const val CHANNEL_ID = "recording"
        private const val NOTIF_ID = 1001

        fun start(ctx: Context, type: ActivityType) =
            send(ctx, ACTION_START) { putExtra(EXTRA_TYPE, type.name) }
        fun pause(ctx: Context) = send(ctx, ACTION_PAUSE)
        fun resume(ctx: Context) = send(ctx, ACTION_RESUME)
        fun stop(ctx: Context) = send(ctx, ACTION_STOP)

        private fun send(ctx: Context, action: String, extra: Intent.() -> Unit = {}) {
            val i = Intent(ctx, RecordingService::class.java).apply {
                this.action = action; extra()
            }
            if (action == ACTION_START && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(i)
            } else {
                ctx.startService(i)
            }
        }
    }
}
