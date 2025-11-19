package com.carlos.makeupsales.ui.screens

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlos.makeupsales.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState(initial = com.carlos.makeupsales.viewmodel.AuthUiState.Idle)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is com.carlos.makeupsales.viewmodel.AuthUiState.Success -> onRegisterSuccess()
            is com.carlos.makeupsales.viewmodel.AuthUiState.Error -> message = (uiState as com.carlos.makeupsales.viewmodel.AuthUiState.Error).message
            else -> {}
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña (>=6)") }, singleLine = true)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { authViewModel.register(email.trim(), password) }, modifier = Modifier.fillMaxWidth()) {
                Text("Crear cuenta")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack) { Text("Volver") }
            Spacer(Modifier.height(12.dp))
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
