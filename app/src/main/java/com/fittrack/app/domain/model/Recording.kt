package com.fittrack.app.domain.model

/** A single GPS sample captured while recording. */
data class GpsPoint(
    val lat: Double,
    val lng: Double,
    val altitude: Double,      // meters
    val speedMps: Float,       // meters/second (from the location fix)
    val timestamp: Long,       // epoch millis
)

/** Sport type being recorded. */
enum class ActivityType(val label: String) {
    RUN("Run"),
    RIDE("Ride"),
    WALK("Walk"),
    HIKE("Hike"),
}

/** Lifecycle of a recording session. */
enum class RecordingStatus { IDLE, RECORDING, PAUSED, SAVING }

/**
 * Live, computed stats shown on the record screen and saved with the activity.
 * All derived from the GpsPoint stream.
 */
data class RecordingStats(
    val status: RecordingStatus = RecordingStatus.IDLE,
    val type: ActivityType = ActivityType.RUN,
    val distanceMeters: Double = 0.0,
    val elapsedTimeMs: Long = 0L,        // total wall-clock since start
    val movingTimeMs: Long = 0L,         // time spent actually moving
    val currentSpeedMps: Float = 0f,
    val elevationGainMeters: Double = 0.0,
    val points: List<GpsPoint> = emptyList(),
) {
    /** Average pace in minutes per kilometer (0 if no distance yet). */
    val avgPaceMinPerKm: Double
        get() = if (distanceMeters > 10) {
            (movingTimeMs / 60000.0) / (distanceMeters / 1000.0)
        } else 0.0

    val avgSpeedMps: Double
        get() = if (movingTimeMs > 0) distanceMeters / (movingTimeMs / 1000.0) else 0.0

    val distanceKm: Double get() = distanceMeters / 1000.0
}
