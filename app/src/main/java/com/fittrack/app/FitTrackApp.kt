package com.fittrack.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. @HiltAndroidApp bootstraps dependency injection
 * for the whole app (same DI approach Strava uses — Hilt).
 */
@HiltAndroidApp
class FitTrackApp : Application()
