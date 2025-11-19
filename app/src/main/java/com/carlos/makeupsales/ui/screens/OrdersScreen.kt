package com.carlos.makeupsales.ui.screens

import com.carlos.makeupsales.ui.components.AppHeader
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.makeupsales.MakeupApplication
import com.carlos.makeupsales.data.local.CustomerEntity
import com.carlos.makeupsales.data.local.OrderEntity
import com.carlos.makeupsales.data.local.OrderItemEntity
import com.carlos.makeupsales.data.local.OrderStatus
import com.carlos.makeupsales.data.local.ProductEntity
import com.carlos.makeupsales.ui.viewmodel.CustomersViewModel
import com.carlos.makeupsales.ui.viewmodel.OrdersViewModel
import com.carlos.makeupsales.ui.viewmodel.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as MakeupApplication

    val ordersViewModel: OrdersViewModel = viewModel(
        factory = OrdersViewModel.Factory(app.database.orderDao(), app.orderRepository)
    )

    val productsViewModel: ProductsViewModel = viewModel(
        factory = ProductsViewModel.Factory(app.productRepository)
    )

    val customersViewModel: CustomersViewModel = viewModel(
        factory = CustomersViewModel.Factory(app.customerRepository)
    )

    val ordersUi by ordersViewModel.uiState.collectAsState()
    val productsUi by productsViewModel.uiState.collectAsState()
    val customersUi by customersViewModel.uiState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // confirmación de borrado
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }

    // detalle de orden
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<OrderEntity?>(null) }
    val selectedOrderItems by ordersViewModel.selectedOrderItems.collectAsState()

    // 🔍 NUEVO: estado del buscador
    var searchQuery by remember { mutableStateOf("") }

    // 🔍 NUEVO: lista filtrada de órdenes
    val filteredOrders = remember(ordersUi.orders, searchQuery) {
        if (searchQuery.isBlank()) {
            ordersUi.orders
        } else {
            val q = searchQuery.trim()
            ordersUi.orders.filter { owc ->
                owc.order.id.toString().contains(q, ignoreCase = true) ||
                        owc.customer.name.contains(q, ignoreCase = true) ||
                        owc.order.status.name.contains(q, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Órdenes",
                subtitle = "Historial de ventas"
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear orden")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // 🔍 NUEVO: campo de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar orden") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    ordersUi.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    filteredOrders.isEmpty() -> {
                        Text(
                            text = if (ordersUi.orders.isEmpty())
                                "No hay órdenes aún. Toca + para crear una."
                            else
                                "No se encontraron órdenes para: \"$searchQuery\"",
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredOrders) { owc ->
                                OrderItemCard(
                                    orderWithCustomer = owc,
                                    onDelete = {
                                        orderToDelete = owc.order
                                        showDeleteConfirm = true
                                    },
                                    onView = {
                                        selectedOrder = owc.order
                                        ordersViewModel.loadOrderItems(owc.order.id)
                                        showDetailDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // diálogo crear orden
    if (showCreateDialog) {
        CreateOrderDialog(
            products = productsUi.products,
            customers = customersUi.customers,
            onDismiss = { showCreateDialog = false },
            onCreate = { customerId, selectedItems ->
                val orderItems = selectedItems.map { (product, qty) ->
                    OrderItemEntity(
                        orderId = 0L,
                        productId = product.id,
                        quantity = qty,
                        unitPrice = product.price,
                        subtotal = product.price * qty
                    )
                }
                ordersViewModel.createOrder(customerId, orderItems)
                showCreateDialog = false
            }
        )
    }

    // diálogo confirmación eliminar
    if (showDeleteConfirm && orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false; orderToDelete = null },
            title = { Text("Eliminar orden") },
            text = {
                Text("¿Seguro que quieres eliminar la orden #${orderToDelete?.id}? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    orderToDelete?.let { ordersViewModel.deleteOrder(it) }
                    showDeleteConfirm = false
                    orderToDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false; orderToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // diálogo detalle de orden
    if (showDetailDialog && selectedOrder != null) {
        OrderDetailDialog(
            order = selectedOrder!!,
            orderItems = selectedOrderItems,
            products = productsUi.products,
            onDismiss = {
                showDetailDialog = false
                selectedOrder = null
            },
            onChangeStatus = { order, newStatus ->
                ordersViewModel.updateOrderStatus(order, newStatus)
                showDetailDialog = false
                selectedOrder = null
            }
        )
    }
}


@Composable
private fun OrderItemCard(
    orderWithCustomer: com.carlos.makeupsales.data.local.OrderWithCustomer,
    onDelete: () -> Unit,
    onView: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Orden #${orderWithCustomer.order.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Cliente: ${orderWithCustomer.customer.name}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Total: C$${orderWithCustomer.order.total}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Estado: ${orderWithCustomer.order.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onView) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "Ver detalle"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Eliminar orden",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderDialog(
    products: List<ProductEntity>,
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onCreate: (customerId: Long, selectedItems: List<Pair<ProductEntity, Int>>) -> Unit
) {
    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    val selectableItems = remember { mutableStateMapOf<Long, Int>() } // productId -> qty

    var openCustomerPicker by remember { mutableStateOf(false) }

    LaunchedEffect(customers.size) {
        if (customers.isNotEmpty() && selectedCustomerId == null) {
            selectedCustomerId = customers.first().id
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val customerId = selectedCustomerId ?: return@TextButton
                val selected = selectableItems.mapNotNull { (pid, qty) ->
                    val p = products.find { it.id == pid }
                    if (p != null && qty > 0) Pair(p, qty) else null
                }
                if (selected.isNotEmpty()) {
                    onCreate(customerId, selected)
                }
            }) {
                Text("Crear orden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Nueva orden") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Cliente", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customers.find { it.id == selectedCustomerId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        placeholder = { Text("Selecciona un cliente") },
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(customers.size) {
                                detectTapGestures {
                                    if (customers.isNotEmpty()) openCustomerPicker = true
                                }
                            }
                    )

                    OutlinedButton(
                        onClick = { if (customers.isNotEmpty()) openCustomerPicker = true },
                        enabled = customers.isNotEmpty(),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("Cambiar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Productos", style = MaterialTheme.typography.bodyMedium)

                if (products.isEmpty()) {
                    Text(
                        "No hay productos disponibles.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                        items(products) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        product.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Precio: C$${product.price}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Stock: ${product.stock}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        val current = selectableItems[product.id] ?: 0
                                        if (current > 0) selectableItems[product.id] = current - 1
                                    }) { Text("-") }
                                    Text(
                                        (selectableItems[product.id] ?: 0).toString(),
                                        modifier = Modifier.width(28.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    IconButton(onClick = {
                                        val current = selectableItems[product.id] ?: 0
                                        if (current < product.stock) selectableItems[product.id] = current + 1
                                    }) { Text("+") }
                                }
                            }
                        }
                    }
                }

                if (openCustomerPicker) {
                    CustomerSelectionDialog(
                        customers = customers,
                        onDismiss = { openCustomerPicker = false },
                        onSelect = {
                            selectedCustomerId = it.id
                            openCustomerPicker = false
                        }
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectionDialog(
    customers: List<CustomerEntity>,
    onDismiss: () -> Unit,
    onSelect: (CustomerEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar cliente") },
        text = {
            if (customers.isEmpty()) {
                Text("No hay clientes disponibles.")
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn {
                        items(customers) { c ->
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(c.id) {
                                        detectTapGestures { onSelect(c) }
                                    }
                                    .padding(vertical = 4.dp),
                                headlineContent = { Text(c.name) },
                                supportingContent = {
                                    val details = listOfNotNull(
                                        c.phone,
                                        c.instagramOrWhatsapp
                                    ).joinToString(" • ")
                                    if (details.isNotBlank()) Text(details)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailDialog(
    order: OrderEntity,
    orderItems: List<OrderItemEntity>,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onChangeStatus: (OrderEntity, OrderStatus) -> Unit
) {
    val total = orderItems.sumOf { it.subtotal }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Orden #${order.id}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ClienteId: ${order.customerId}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Items:", style = MaterialTheme.typography.titleSmall)
                if (orderItems.isEmpty()) {
                    Text(
                        "No hay items registrados para esta orden.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        orderItems.forEach { itItem ->
                            val productName = products.find { p ->
                                p.id == itItem.productId
                            }?.name ?: "Producto #${itItem.productId}"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = productName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Cantidad: ${itItem.quantity} • Precio: C$${itItem.unitPrice}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "Subtotal: C$${itItem.subtotal}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Divider()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Total calculado: C$${total}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estado actual: ${order.status}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onChangeStatus(order, OrderStatus.PAID) }) {
                    Text("Marcar como PAGADA")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { onChangeStatus(order, OrderStatus.CANCELED) }) {
                    Text("Cancelar orden")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
