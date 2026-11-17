package com.fittrack.app.ui.screens.record

import android.content.Context
import androidx.lifecycle.ViewModel
import com.fittrack.app.data.recording.RecordingManager
import com.fittrack.app.data.recording.RecordingService
import com.fittrack.app.domain.model.ActivityType
import com.fittrack.app.domain.model.RecordingStats
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    recordingManager: RecordingManager,
) : ViewModel() {

    /** Live stats observed by the UI (driven by RecordingManager via the service). */
    val stats: StateFlow<RecordingStats> = recordingManager.stats

    fun start(type: ActivityType) = RecordingService.start(appContext, type)
    fun pause() = RecordingService.pause(appContext)
    fun resume() = RecordingService.resume(appContext)
    fun stop() = RecordingService.stop(appContext)
}
