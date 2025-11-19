package com.carlos.makeupsales.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.makeupsales.MakeupApplication
import com.carlos.makeupsales.data.local.OrderStatus
import com.carlos.makeupsales.ui.viewmodel.CustomersViewModel
import com.carlos.makeupsales.ui.viewmodel.OrdersViewModel
import com.carlos.makeupsales.ui.viewmodel.ProductsViewModel

@Composable
fun DashboardScreen(
    onLogout: () -> Unit,
    onOpenAbout: () -> Unit
    // si quieres, puedes darle = {} como default
) {
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

    // --- KPIs calculados en memoria ---
    val totalSales = remember(ordersUi.orders) {
        ordersUi.orders
            .filter { it.order.status == OrderStatus.PAID }
            .sumOf { it.order.total }
    }

    val pendingOrders = remember(ordersUi.orders) {
        ordersUi.orders.count { it.order.status == OrderStatus.PENDING }
    }

    val totalCustomers = customersUi.customers.size

    val lowStockCount = remember(productsUi.products) {
        productsUi.products.count { it.stock <= 5 } // umbral de stock bajo
    }

    val lastOrdersAmounts = remember(ordersUi.orders) {
        ordersUi.orders
            .takeLast(5)
            .map { it.order.total.toDouble() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Encabezado con título + Logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MakeUpSales Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Row( // 👈 fila solo para los botones de la derecha
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenAbout) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Créditos",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                FilledTonalButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Salir",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Resumen general de tu negocio:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- KPIs en grid 2x2 ---
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Ventas totales",
                    value = "C$${"%.2f".format(totalSales)}",
                    icon = Icons.Filled.Money,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Órdenes pendientes",
                    value = pendingOrders.toString(),
                    icon = Icons.Filled.PendingActions,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Clientes",
                    value = totalCustomers.toString(),
                    icon = Icons.Filled.Group,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Stock bajo",
                    value = lowStockCount.toString(),
                    icon = Icons.Filled.Inventory2,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Últimas órdenes",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (lastOrdersAmounts.isEmpty()) {
                    Text(
                        text = "Todavía no hay órdenes suficientes para mostrar un gráfico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    SalesBarChart(
                        amounts = lastOrdersAmounts,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun SalesBarChart(
    amounts: List<Double>,
    modifier: Modifier = Modifier
) {
    val max = (amounts.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    val maxHeight = 120.dp

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fila con barras + monto encima
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            amounts.forEachIndexed { index, value ->
                val ratio = (value / max).toFloat()
                val barHeight = maxHeight * ratio

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // 👇 Monto numérico arriba de la barra
                    Text(
                        text = "C$${"%.0f".format(value)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Etiqueta de orden debajo
                    Text(
                        text = "O${index + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Monto de las últimas ${amounts.size} órdenes",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
