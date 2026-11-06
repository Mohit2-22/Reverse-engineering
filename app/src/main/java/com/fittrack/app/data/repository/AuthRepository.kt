package com.fittrack.app.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps Supabase Auth. Replaces Strava's OAuth flow with email/password
 * (simplest for a learning app; Google login can be added later).
 */
@Singleton
class AuthRepository @Inject constructor(
    private val client: SupabaseClient,
) {
    /** Emits the current session state (used to decide login vs home). */
    val sessionStatus: Flow<SessionStatus> = client.auth.sessionStatus

    val isLoggedIn: Boolean
        get() = client.auth.currentSessionOrNull() != null

    suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }
}
