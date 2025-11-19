package com.carlos.makeupsales.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlos.makeupsales.utils.FirebaseAuthManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment

@Composable
fun MainScreen(onLogout: () -> Unit, openOrderDetail: (String) -> Unit) {
    val user = FirebaseAuthManager.currentUser()
    val email = user?.email ?: "Invitado"

    var orders by remember { mutableStateOf(listOf<Pair<String, String>>()) } // Pair(orderId, productId)

    LaunchedEffect(user) {
        if (user != null) {
            FirebaseAuthManager.firestore.collection("orders")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { snap ->
                    val list = snap.documents.mapNotNull { doc ->
                        val pid = doc.getString("productId")
                        val id = doc.id
                        if (pid != null) id to pid else null
                    }
                    orders = list
                }
                .addOnFailureListener { /* no action */ }
        } else {
            orders = emptyList()
        }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Hola, $email", style = MaterialTheme.typography.titleLarge)
                    Text("Mis órdenes", style = MaterialTheme.typography.bodyMedium)
                }
                Button(onClick = onLogout) { Text("Logout") }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn {
                items(orders) { (orderId, productId) ->
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Order: $orderId")
                                Text("Product ID: $productId", style = MaterialTheme.typography.bodySmall)
                            }
                            Button(onClick = { openOrderDetail(orderId) }) { Text("Ver") }
                        }
                    }
                }
            }
        }
    }
}
