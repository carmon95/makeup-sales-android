package com.carlos.makeupsales.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carlos.makeupsales.utils.FirebaseAuthManager

@Composable
fun OrderDetailScreen(orderId: String, onBack: () -> Unit) {
    var productId by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf<Long?>(null) }
    var productName by remember { mutableStateOf<String?> (null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(orderId) {
        if (orderId.isEmpty()) {
            error = "OrderId vacío"
            loading = false
            return@LaunchedEffect
        }
        loading = true
        FirebaseAuthManager.firestore.collection("orders").document(orderId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    productId = doc.getString("productId")
                    quantity = doc.getLong("quantity")
                    productId?.let { pid ->
                        FirebaseAuthManager.firestore.collection("products").document(pid)
                            .get()
                            .addOnSuccessListener { pdoc ->
                                if (pdoc.exists()) {
                                    productName = pdoc.getString("name")
                                } else {
                                    productName = "Producto no encontrado"
                                }
                                loading = false
                            }
                            .addOnFailureListener { e ->
                                error = e.message
                                loading = false
                            }
                    } ?: run {
                        error = "productId inválido en la orden"
                        loading = false
                    }
                } else {
                    error = "Orden no existe"
                    loading = false
                }
            }
            .addOnFailureListener { e ->
                error = e.message
                loading = false
            }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(padding), verticalArrangement = Arrangement.Top) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Detalle de orden", style = MaterialTheme.typography.headlineSmall)
                Button(onClick = onBack) { Text("Volver") }
            }
            Spacer(Modifier.height(12.dp))
            if (loading) {
                CircularProgressIndicator()
            } else {
                error?.let {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                } ?: run {
                    Text("Producto: ${productName ?: "—"}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Cantidad: ${quantity ?: 1}")
                    Spacer(Modifier.height(8.dp))
                    Text("Product ID: ${productId ?: "—"}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
