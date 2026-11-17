package com.fittrack.app.ui.screens.record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fittrack.app.domain.model.ActivityType
import com.fittrack.app.domain.model.RecordingStatus

@Composable
fun RecordScreen(vm: RecordViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val stats by vm.stats.collectAsStateWithLifecycle()

    // --- permission state ---
    fun hasLocation() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    var granted by remember { mutableStateOf(hasLocation()) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    fun requestPerms() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms += Manifest.permission.POST_NOTIFICATIONS
        }
        permLauncher.launch(perms.toTypedArray())
    }

    var selectedType by remember { mutableStateOf(ActivityType.RUN) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!granted) {
            PermissionPrompt(onGrant = ::requestPerms)
            return
        }

        // Activity type selector (only before/while idle)
        if (stats.status == RecordingStatus.IDLE) {
            TypeSelector(selected = selectedType, onSelect = { selectedType = it })
            Spacer(Modifier.height(24.dp))
        } else {
            Text(
                stats.type.label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(16.dp))
        }

        // --- BIG distance ---
        StatBig(value = "%.2f".format(stats.distanceKm), unit = "km")
        Spacer(Modifier.height(24.dp))

        // --- secondary stats row ---
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatSmall("Time", formatDuration(stats.elapsedTimeMs))
            StatSmall("Pace", formatPace(stats.avgPaceMinPerKm))
            StatSmall("Elev", "${stats.elevationGainMeters.toInt()} m")
        }
        Spacer(Modifier.height(40.dp))

        // --- controls ---
        when (stats.status) {
            RecordingStatus.IDLE ->
                BigButton("Start", Icons.Filled.PlayArrow, MaterialTheme.colorScheme.primary) {
                    vm.start(selectedType)
                }
            RecordingStatus.RECORDING -> Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BigButton("Pause", Icons.Filled.Pause, MaterialTheme.colorScheme.secondary) { vm.pause() }
                BigButton("Finish", Icons.Filled.Stop, MaterialTheme.colorScheme.error) { vm.stop() }
            }
            RecordingStatus.PAUSED -> Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BigButton("Resume", Icons.Filled.PlayArrow, MaterialTheme.colorScheme.primary) { vm.resume() }
                BigButton("Finish", Icons.Filled.Stop, MaterialTheme.colorScheme.error) { vm.stop() }
            }
            RecordingStatus.SAVING ->
                Text("Saving…", style = MaterialTheme.typography.titleMedium)
        }

        if (stats.points.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                "${stats.points.size} GPS points",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionPrompt(onGrant: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.LocationOn, null, Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("Location needed to record", style = MaterialTheme.typography.titleMedium)
        Text(
            "FitTrack uses GPS to track your route, distance and pace.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGrant) { Text("Grant location access") }
    }
}

@Composable
private fun TypeSelector(selected: ActivityType, onSelect: (ActivityType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ActivityType.entries.forEach { t ->
            FilterChip(
                selected = selected == t,
                onClick = { onSelect(t) },
                label = { Text(t.label) },
            )
        }
    }
}

@Composable
private fun StatBig(value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 72.sp, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
        Text(unit, style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatSmall(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BigButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier.height(56.dp),
    ) {
        Icon(icon, null)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

// --- formatters ---
private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun formatPace(minPerKm: Double): String {
    if (minPerKm <= 0 || minPerKm.isInfinite()) return "--'--\""
    val m = minPerKm.toInt()
    val s = ((minPerKm - m) * 60).toInt()
    return "%d'%02d\"".format(m, s)
}
