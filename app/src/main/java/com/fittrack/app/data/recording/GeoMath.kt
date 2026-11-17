package com.fittrack.app.data.recording

import com.fittrack.app.domain.model.GpsPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Geo calculations for turning a GPS point stream into distance/elevation. */
object GeoMath {

    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Great-circle distance between two points in meters (haversine). */
    fun distanceMeters(a: GpsPoint, b: GpsPoint): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)
        val lat1 = Math.toRadians(a.lat)
        val lat2 = Math.toRadians(b.lat)
        val h = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
        return 2 * EARTH_RADIUS_M * atan2(sqrt(h), sqrt(1 - h))
    }

    /**
     * A GPS fix is "noise" if it jumped an implausible distance for the time gap
     * (e.g. urban canyon drift). We drop points faster than ~12 m/s unless the
     * device itself reported a high speed. Keeps the track clean.
     */
    fun isPlausible(prev: GpsPoint, next: GpsPoint): Boolean {
        val dtSec = (next.timestamp - prev.timestamp) / 1000.0
        if (dtSec <= 0) return false
        val d = distanceMeters(prev, next)
        val impliedSpeed = d / dtSec
        // allow up to 25 m/s (90 km/h, covers cycling) or trust a strong fix
        return impliedSpeed <= 25.0 || next.speedMps > 2f
    }

    /** Positive elevation change only (ignores descents and tiny jitter). */
    fun elevationGain(prev: GpsPoint, next: GpsPoint): Double {
        val delta = next.altitude - prev.altitude
        return if (delta > 1.0) delta else 0.0   // 1m threshold filters GPS altitude noise
    }
}
