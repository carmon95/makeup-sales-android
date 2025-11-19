package com.carlos.makeupsales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState


// Tus pantallas existentes
import com.carlos.makeupsales.ui.screens.DashboardScreen
import com.carlos.makeupsales.ui.screens.OrdersScreen
import com.carlos.makeupsales.ui.screens.ProductsScreen
import com.carlos.makeupsales.ui.screens.CustomersScreen
import com.carlos.makeupsales.ui.theme.MakeupSalesTheme

// Nuevas importaciones que añadí para autenticación (asegúrate de tener estos archivos)
import com.carlos.makeupsales.ui.screens.LoginScreen
import com.carlos.makeupsales.ui.screens.RegisterScreen
import com.carlos.makeupsales.viewmodel.AuthViewModel
import com.carlos.makeupsales.ui.navigation.Routes
import com.carlos.makeupsales.ui.screens.AboutScreen
import com.carlos.makeupsales.viewmodel.AuthUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MakeupSalesTheme {
                AppRoot()
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val label: String, val icon: Int) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", android.R.drawable.ic_menu_myplaces)
    object Orders : BottomNavItem("orders", "Orders", android.R.drawable.ic_menu_agenda)
    object Customers : BottomNavItem("customers", "Customers", android.R.drawable.ic_menu_manage)
    object Products : BottomNavItem("products", "Products", android.R.drawable.ic_menu_sort_by_size)
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Orders,
        BottomNavItem.Customers,
        BottomNavItem.Products
    )

    // ViewModel de auth (usado por las pantallas Login/Register)
    val authVM: AuthViewModel = viewModel()

    // 👇 Observamos el estado de autenticación del ViewModel
    val authState by authVM.uiState.collectAsState()

    // 👇 Hay sesión SOLO si el estado es Success
    val isLoggedIn = authState is AuthUiState.Success

    // 👇 Ruta inicial según si hay sesión o no
    val startDestination = if (isLoggedIn) {
        BottomNavItem.Dashboard.route
    } else {
        Routes.LOGIN
    }

    Scaffold(
        bottomBar = {
            if (isLoggedIn) {   // 👈 SOLO mostrar bottom bar si hay sesión
                // 👇 NUEVO: fondo con esquinas redondeadas y sombra
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        items.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        painter = painterResource(id = item.icon),
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) },
                                // 👇 Colores más vivos para item seleccionado / no seleccionado
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        // aquí sigue tu NavHost igual

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            // --- Rutas de autenticación (añadidas) ---
            composable(Routes.LOGIN) {
                LoginScreen(
                    authViewModel = authVM,
                    onGoToRegister = { navController.navigate(Routes.REGISTER) },
                    onLoginSuccess = {
                        // Al iniciar sesión, vamos al dashboard y limpiamos backstack de login
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    authViewModel = authVM,
                    onRegisterSuccess = {
                        // Al crear cuenta, vamos al dashboard y limpiamos backstack de register
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // DASHBOARD (privado)
            composable(BottomNavItem.Dashboard.route) {
                if (isLoggedIn) {
                    DashboardScreen(
                        onLogout = {
                            authVM.signOut()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0)
                            }
                        },
                        onOpenAbout = {
                            navController.navigate(Routes.ABOUT)
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            }

// ORDERS (privado)
            composable(BottomNavItem.Orders.route) {
                if (isLoggedIn) {
                    OrdersScreen()
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            }

// CUSTOMERS (privado)
            composable(BottomNavItem.Customers.route) {
                if (isLoggedIn) {
                    CustomersScreen()
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            }

// PRODUCTS (privado)
            composable(BottomNavItem.Products.route) {
                if (isLoggedIn) {
                    ProductsScreen()
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            }

            // ABOUT (privado)
            composable(Routes.ABOUT) {
                if (isLoggedIn) {
                    AboutScreen(
                        onBack = {
                            navController.popBackStack()  // vuelve a la pantalla anterior (Dashboard)
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)
                        }
                    }
                }
            }
        }
    }
}
