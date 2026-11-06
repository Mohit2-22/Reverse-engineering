package com.fittrack.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fittrack.app.ui.screens.HomeScreen
import com.fittrack.app.ui.screens.auth.LoginScreen
import com.fittrack.app.ui.screens.auth.SignupScreen

/**
 * App-level navigation: Auth (login/signup) ↔ Home.
 * @param startLoggedIn decides the start destination based on Supabase session.
 */
@Composable
fun FitTrackNavHost(startLoggedIn: Boolean) {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = if (startLoggedIn) Routes.HOME else Routes.LOGIN,
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToSignup = { nav.navigate(Routes.SIGNUP) },
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignedUp = {
                    nav.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onGoToLogin = { nav.popBackStack() },
            )
        }
        composable(Routes.HOME) { HomeScreen() }
    }
}
