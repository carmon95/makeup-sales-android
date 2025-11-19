package com.carlos.makeupsales.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carlos.makeupsales.data.local.OrderEntity
import com.carlos.makeupsales.data.local.OrderItemEntity
import com.carlos.makeupsales.data.local.OrderStatus
import com.carlos.makeupsales.data.local.OrderDao
import com.carlos.makeupsales.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OrdersUiState(
    val orders: List<com.carlos.makeupsales.data.local.OrderWithCustomer> = emptyList(),
    val isLoading: Boolean = false
)

class OrdersViewModel(
    private val orderDao: OrderDao,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState(isLoading = true))
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    // StateFlow que expone los items de la orden seleccionada
    private val _selectedOrderItems = MutableStateFlow<List<OrderItemEntity>>(emptyList())
    val selectedOrderItems: StateFlow<List<OrderItemEntity>> = _selectedOrderItems.asStateFlow()

    init {
        viewModelScope.launch {
            orderDao.getAllOrdersWithCustomer().collectLatest { list ->
                _uiState.value = OrdersUiState(orders = list, isLoading = false)
            }
        }
    }

    fun createOrder(
        customerId: Long,
        items: List<OrderItemEntity>
    ) {
        viewModelScope.launch {
            // total computed from items
            val total = items.sumOf { it.subtotal }
            val order = OrderEntity(
                dateMillis = System.currentTimeMillis(),
                customerId = customerId,
                status = OrderStatus.PENDING,
                total = total
            )
            orderRepository.createOrderWithItems(order, items)
        }
    }

    /**
     * Borra una orden y sus items (usa el DAO que implementa la transacción).
     */
    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            try {
                orderDao.deleteOrderWithItems(order)
            } catch (e: Exception) {
                // opcional: manejar error, p. ej. log o exponer estado de error
                e.printStackTrace()
            }
        }
    }

    /**
     * Carga (desde repository) los items de la orden seleccionada y los publica en selectedOrderItems.
     */
    fun loadOrderItems(orderId: Long) {
        viewModelScope.launch {
            try {
                val items = orderRepository.getItemsForOrder(orderId)
                _selectedOrderItems.value = items
            } catch (e: Exception) {
                e.printStackTrace()
                _selectedOrderItems.value = emptyList()
            }
        }
    }

    /**
     * Actualiza el estado de la orden (ej: PENDING -> PAID / CANCELED).
     * Asume que orderDao.updateOrder(order) está implementado en tu DAO.
     */
    fun updateOrderStatus(order: OrderEntity, newStatus: OrderStatus) {
        viewModelScope.launch {
            try {
                val updated = order.copy(status = newStatus)
                orderDao.updateOrder(updated)
                // No es necesario llamar explícitamente a loadOrders() porque
                // el Flow de getAllOrdersWithCustomer() emitirá los cambios.
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class Factory(
        private val orderDao: OrderDao,
        private val orderRepository: OrderRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrdersViewModel::class.java)) {
                return OrdersViewModel(orderDao, orderRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
