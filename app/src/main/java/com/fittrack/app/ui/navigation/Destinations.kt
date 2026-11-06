package com.fittrack.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector

/** Top-level route constants. */
object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"          // hosts the bottom-nav graph

    const val FEED = "feed"
    const val MAP = "map"
    const val RECORD = "record"
    const val PROFILE = "profile"
}

/** Bottom navigation tabs (mirrors Strava's Feed / Map / Record / Profile layout). */
enum class BottomTab(val route: String, val label: String, val icon: ImageVector) {
    Feed(Routes.FEED, "Feed", Icons.Filled.Home),
    Map(Routes.MAP, "Map", Icons.Filled.Map),
    Record(Routes.RECORD, "Record", Icons.Filled.Add),
    Profile(Routes.PROFILE, "Profile", Icons.Filled.AccountCircle),
}
