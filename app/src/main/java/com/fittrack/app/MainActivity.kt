package com.fittrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fittrack.app.data.repository.AuthRepository
import com.fittrack.app.ui.navigation.FitTrackNavHost
import com.fittrack.app.ui.theme.FitTrackTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The single Activity for the whole app (same pattern as Strava).
 * All screens are Compose destinations hosted here.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startLoggedIn = authRepository.isLoggedIn

        setContent {
            FitTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FitTrackNavHost(startLoggedIn = startLoggedIn)
                }
            }
        }
    }
}
