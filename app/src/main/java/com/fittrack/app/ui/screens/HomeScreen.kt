package com.fittrack.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fittrack.app.ui.navigation.BottomTab

/**
 * Home scaffold = single host with a bottom nav bar and a nested NavHost.
 * This mirrors Strava's single-activity + bottom-tab structure.
 */
@Composable
fun HomeScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStack by navController.currentBackStackEntryAsState()
                val currentDest = backStack?.destination
                BottomTab.entries.forEach { tab ->
                    val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Feed.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(BottomTab.Feed.route) { FeedScreen() }
            composable(BottomTab.Map.route) { MapScreen() }
            composable(BottomTab.Record.route) { RecordScreen() }
            composable(BottomTab.Profile.route) { ProfileScreen() }
        }
    }
}
