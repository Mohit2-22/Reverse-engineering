package com.fittrack.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sprint-1 placeholders. Each becomes a real screen in later sprints:
 *  - Feed    → activity list (Sprint 4)
 *  - Map     → explore / routes (Sprint 3)
 *  - Record  → GPS recording (Sprint 2)  ← the core engine
 *  - Profile → user stats + sign out (Sprint 4)
 */
@Composable
private fun Placeholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable fun FeedScreen() = Placeholder("Feed", "Your activities will appear here (Sprint 4)")
@Composable fun MapScreen() = Placeholder("Map", "Explore & routes (Sprint 3)")
@Composable fun RecordScreen() = Placeholder("Record", "GPS workout recording (Sprint 2)")
@Composable fun ProfileScreen() = Placeholder("Profile", "Your stats & settings (Sprint 4)")
