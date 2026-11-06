package com.fittrack.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Shared form for both login and signup. */
@Composable
private fun AuthForm(
    title: String,
    primaryLabel: String,
    altLabel: String,
    onPrimary: (String, String) -> Unit,
    onAlt: () -> Unit,
    vm: AuthViewModel,
    onSuccess: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.success) { if (state.success) onSuccess() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true, modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onPrimary(email.trim(), password) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.loading) CircularProgressIndicator(Modifier.size(20.dp))
            else Text(primaryLabel)
        }
        TextButton(onClick = onAlt) { Text(altLabel) }
    }
}

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoToSignup: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) = AuthForm(
    title = "Welcome to FitTrack",
    primaryLabel = "Log in",
    altLabel = "New here? Create an account",
    onPrimary = vm::signIn,
    onAlt = onGoToSignup,
    vm = vm,
    onSuccess = onLoggedIn,
)

@Composable
fun SignupScreen(
    onSignedUp: () -> Unit,
    onGoToLogin: () -> Unit,
    vm: AuthViewModel = hiltViewModel(),
) = AuthForm(
    title = "Create your account",
    primaryLabel = "Sign up",
    altLabel = "Already have an account? Log in",
    onPrimary = vm::signUp,
    onAlt = onGoToLogin,
    vm = vm,
    onSuccess = onSignedUp,
)
