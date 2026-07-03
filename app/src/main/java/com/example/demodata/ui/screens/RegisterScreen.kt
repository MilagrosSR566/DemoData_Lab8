package com.example.demodata.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack:   () -> Unit,
    onSubmit: (email: String, pass: String, onResult: (Boolean) -> Unit) -> Unit
) {
    var email                   by remember { mutableStateOf("") }
    var password                by remember { mutableStateOf("") }
    var confirmPassword         by remember { mutableStateOf("") }
    var passwordVisible         by remember { mutableStateOf(false) }
    var confirmPasswordVisible  by remember { mutableStateOf(false) }
    var loading                 by remember { mutableStateOf(false) }
    var error                   by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title            = { Text("Nuevo Usuario") },
                navigationIcon   = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Email") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                enabled       = !loading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value               = password,
                onValueChange       = { password = it },
                label               = { Text("Contraseña") },
                modifier            = Modifier.fillMaxWidth(),
                singleLine          = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon        = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                enabled = !loading
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value               = confirmPassword,
                onValueChange       = { confirmPassword = it },
                label               = { Text("Confirmar Contraseña") },
                modifier            = Modifier.fillMaxWidth(),
                singleLine          = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon        = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                    }
                },
                enabled = !loading
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick  = {
                    if (password != confirmPassword) {
                        error = "Las contraseñas no coinciden"
                        return@Button
                    }
                    error   = ""
                    loading = true
                    onSubmit(email, password) { success ->
                        loading = false
                        if (!success) error = "Error al registrar. Intente con otro email."
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled  = !loading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Crear cuenta")
                }
            }
        }
    }
}