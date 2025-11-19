package com.carlos.makeupsales.data.repository

import com.carlos.makeupsales.data.local.OrderDao
import com.carlos.makeupsales.data.local.OrderEntity
import com.carlos.makeupsales.data.local.OrderItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OrderRepository(
    private val orderDao: OrderDao
) {

    /**
     * Crea una orden con sus items y actualiza stock en una sola transacción
     * delegada al DAO (insertOrderWithItemsAndUpdateStock).
     * Devuelve el id de la orden creada.
     */
    suspend fun createOrderWithItems(
        order: OrderEntity,
        items: List<OrderItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        orderDao.insertOrderWithItemsAndUpdateStock(order, items)
    }

    /**
     * Obtiene los items asociados a una orden (por orderId).
     */
    suspend fun getItemsForOrder(orderId: Long): List<OrderItemEntity> = withContext(Dispatchers.IO) {
        orderDao.getItemsByOrderId(orderId)
    }
}
