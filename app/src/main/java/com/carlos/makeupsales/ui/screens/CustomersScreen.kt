package com.carlos.makeupsales.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.carlos.makeupsales.ui.components.AppHeader
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.makeupsales.MakeupApplication
import com.carlos.makeupsales.data.local.CustomerEntity
import com.carlos.makeupsales.ui.viewmodel.CustomersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as MakeupApplication
    val viewModel: CustomersViewModel = viewModel(
        factory = CustomersViewModel.Factory(app.customerRepository)
    )

    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    // 👉 NUEVO: estado del buscador
    var searchQuery by remember { mutableStateOf("") }

    // 👉 NUEVO: clientes filtrados
    val filteredCustomers = remember(uiState.customers, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.customers
        } else {
            val q = searchQuery.trim()
            uiState.customers.filter { c ->
                c.name.contains(q, ignoreCase = true) ||
                        (c.phone?.contains(q, ignoreCase = true) == true) ||
                        (c.instagramOrWhatsapp?.contains(q, ignoreCase = true) == true) ||
                        (c.address?.contains(q, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Clientes",
                subtitle = "Tus clientas frecuentes"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // 👉 NUEVO: buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar cliente") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    filteredCustomers.isEmpty() -> {
                        Text(
                            text = if (uiState.customers.isEmpty())
                                "No hay clientes aún. Toca + para agregar."
                            else
                                "No se encontraron clientes para: \"$searchQuery\"",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        CustomerList(
                            customers = filteredCustomers,
                            onDelete = { viewModel.deleteCustomer(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddCustomerDialog(
            onDismiss = { showDialog = false },
            onSave = { name, phone, insta, address ->
                viewModel.saveCustomer(name, phone, insta, address)
                showDialog = false
            }
        )
    }
}

@Composable
private fun CustomerList(
    customers: List<CustomerEntity>,
    onDelete: (CustomerEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(customers) { customer ->
            CustomerItemCard(
                customer = customer,
                onDelete = { onDelete(customer) }
            )
        }
    }
}

@Composable
private fun CustomerItemCard(
    customer: CustomerEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar redondo con la inicial del nombre
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!customer.phone.isNullOrBlank()) {
                    Text(
                        text = "Tel: ${customer.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }

                if (!customer.instagramOrWhatsapp.isNullOrBlank()) {
                    Text(
                        text = "IG/WA: ${customer.instagramOrWhatsapp}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (!customer.address.isNullOrBlank()) {
                    Text(
                        text = "Dir: ${customer.address}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Eliminar cliente",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String?, insta: String?, address: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var insta by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(
                        name,
                        phone.ifBlank { null },
                        insta.ifBlank { null },
                        address.ifBlank { null }
                    )
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = { Text("Nuevo cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono (opcional)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    )
                )
                OutlinedTextField(
                    value = insta,
                    onValueChange = { insta = it },
                    label = { Text("Instagram/WhatsApp (opcional)") }
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Dirección (opcional)") }
                )
            }
        }
    )
}
