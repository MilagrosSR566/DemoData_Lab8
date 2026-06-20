package com.example.demodata.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onSubmit: (username: String, password: String, onResult: (Boolean) -> Unit) -> Unit
) {
    var usuario    by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var error      by remember { mutableStateOf("") }
    var verificando by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
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
            value         = usuario,
            onValueChange = { usuario = it },
            label         = { Text("Usuario") },
            singleLine    = true,
            enabled       = !verificando,
            modifier      = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value               = password,
            onValueChange       = { password = it },
            label               = { Text("Contraseña") },
            singleLine          = true,
            enabled             = !verificando,
            visualTransformation = PasswordVisualTransformation(),
            modifier            = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                error      = ""
                verificando = true
                onSubmit(usuario, password) { ok ->
                    verificando = false
                    if (!ok) error = "Credenciales incorrectas. Pruebe jkn/jkn."
                }
            },
            enabled  = !verificando && usuario.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (verificando) {
                CircularProgressIndicator(
                    modifier    = Modifier.height(24.dp),
                    strokeWidth = 2.dp,
                    color       = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Ingresar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Credenciales por defecto: jkn / jkn",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}