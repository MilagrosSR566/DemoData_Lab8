package com.example.demodata.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext                                    // ← NUEVO
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager                                       // ← NUEVO
import androidx.credentials.CustomCredential                                       // ← NUEVO
import androidx.credentials.GetCredentialRequest                                   // ← NUEVO
import com.google.android.libraries.identity.googleid.GetGoogleIdOption            // ← NUEVO
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential      // ← NUEVO
import com.example.demodata.data.remote.NetworkConstants                        // ← NUEVO
import kotlinx.coroutines.launch                                                   // ← NUEVO

@Composable
fun LoginScreen(
    onSubmit: (username: String, password: String, onResult: (Boolean) -> Unit) -> Unit,
    onGoogleLogin: (token: String, onResult: (Boolean) -> Unit) -> Unit,           // ← NUEVO
    onRegisterNavigate: () -> Unit
) {
    val context = LocalContext.current                                              // ← NUEVO
    val scope = rememberCoroutineScope()                                            // ← NUEVO
    val credentialManager = CredentialManager.create(context)                       // ← NUEVO

    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var verificando by remember { mutableStateOf(false) }

    // ── NUEVO: flujo completo de Google Sign-In ──
    fun handleGoogleLogin() {
        scope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(NetworkConstants.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    verificando = true
                    onGoogleLogin(idToken) { success ->
                        verificando = false
                        if (!success) error = "Error al autenticar con Google"
                    }
                }
            } catch (e: Exception) {
                error = "Cancelado o error: ${e.message}"
            }
        }
    }
    // ── FIN NUEVO ──

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "DemoData",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Sistema de gestión de datos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Email") },
            singleLine = true,
            enabled = !verificando,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !verificando,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility
                else Icons.Default.VisibilityOff
                val description = if (passwordVisible) "Ocultar contraseña"
                else "Mostrar contraseña"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = description)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                error = ""
                verificando = true
                onSubmit(usuario, password) { ok ->
                    verificando = false
                    if (!ok) error = "Credenciales incorrectas. Revisa tu email y contraseña."
                }
            },
            enabled = !verificando && usuario.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (verificando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Ingresar")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── NUEVO: botón de Google Sign-In ──
        OutlinedButton(
            onClick = { handleGoogleLogin() },
            enabled = !verificando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continuar con Google")
        }
        // ── FIN NUEVO ──

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onRegisterNavigate,
            enabled = !verificando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Registrar usuario")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Usa tus credenciales de Platform API",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}