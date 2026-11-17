package com.fittrack.app.data.recording

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.fittrack.app.domain.model.ActivityType
import com.fittrack.app.domain.model.GpsPoint
import com.fittrack.app.domain.model.RecordingStats
import com.fittrack.app.domain.model.RecordingStatus
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of truth for an in-progress recording.
 *
 * Holds the live [RecordingStats] as a StateFlow that both the foreground
 * [RecordingService] and the UI observe. Receives GPS fixes from the
 * FusedLocationProvider, filters noise, and accumulates distance / time /
 * elevation. This is FitTrack's equivalent of Strava's `recording` module core.
 */
@Singleton
class RecordingManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fused: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _stats = MutableStateFlow(RecordingStats())
    val stats: StateFlow<RecordingStats> = _stats.asStateFlow()

    // wall-clock bookkeeping
    private var startElapsedRef = 0L      // SystemClock-ish anchor for elapsed time
    private var lastResumeWallMs = 0L
    private var accumulatedElapsedMs = 0L
    private var lastPoint: GpsPoint? = null

    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            onNewLocation(
                GpsPoint(
                    lat = loc.latitude,
                    lng = loc.longitude,
                    altitude = if (loc.hasAltitude()) loc.altitude else 0.0,
                    speedMps = if (loc.hasSpeed()) loc.speed else 0f,
                    timestamp = System.currentTimeMillis(),
                )
            )
        }
    }

    @SuppressLint("MissingPermission") // caller checks location permission before start
    fun start(type: ActivityType) {
        if (_stats.value.status == RecordingStatus.RECORDING) return
        resetIfIdle(type)
        lastResumeWallMs = System.currentTimeMillis()
        _stats.update { it.copy(status = RecordingStatus.RECORDING, type = type) }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(0f)
            .build()
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    fun pause() {
        if (_stats.value.status != RecordingStatus.RECORDING) return
        accumulatedElapsedMs += System.currentTimeMillis() - lastResumeWallMs
        fused.removeLocationUpdates(callback)
        _stats.update { it.copy(status = RecordingStatus.PAUSED) }
    }

    @SuppressLint("MissingPermission")
    fun resume() {
        if (_stats.value.status != RecordingStatus.PAUSED) return
        lastResumeWallMs = System.currentTimeMillis()
        _stats.update { it.copy(status = RecordingStatus.RECORDING) }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .build()
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    /** Stops updates and returns the final stats. Does NOT reset (caller saves first). */
    fun stop(): RecordingStats {
        if (_stats.value.status == RecordingStatus.RECORDING) {
            accumulatedElapsedMs += System.currentTimeMillis() - lastResumeWallMs
        }
        fused.removeLocationUpdates(callback)
        val final = _stats.value.copy(
            status = RecordingStatus.SAVING,
            elapsedTimeMs = accumulatedElapsedMs,
        )
        _stats.value = final
        return final
    }

    /** Clear everything back to idle (call after a save or discard). */
    fun reset() {
        lastPoint = null
        accumulatedElapsedMs = 0L
        lastResumeWallMs = 0L
        _stats.value = RecordingStats()
    }

    private fun resetIfIdle(type: ActivityType) {
        if (_stats.value.status == RecordingStatus.IDLE) {
            reset()
            _stats.value = RecordingStats(type = type)
        }
    }

    private fun onNewLocation(point: GpsPoint) {
        _stats.update { s ->
            if (s.status != RecordingStatus.RECORDING) return@update s

            val prev = lastPoint
            var newDistance = s.distanceMeters
            var newElevation = s.elevationGainMeters
            var movingTime = s.movingTimeMs

            if (prev != null) {
                if (!GeoMath.isPlausible(prev, point)) return@update s  // drop noise
                val seg = GeoMath.distanceMeters(prev, point)
                // only count as "moving" if we actually displaced > 0.5m
                if (seg > 0.5) {
                    newDistance += seg
                    newElevation += GeoMath.elevationGain(prev, point)
                    movingTime += (point.timestamp - prev.timestamp)
                }
            }
            lastPoint = point

            val elapsed = accumulatedElapsedMs +
                    (System.currentTimeMillis() - lastResumeWallMs)

            s.copy(
                distanceMeters = newDistance,
                elevationGainMeters = newElevation,
                movingTimeMs = movingTime,
                elapsedTimeMs = elapsed,
                currentSpeedMps = point.speedMps,
                points = s.points + point,
            )
        }
    }
}
