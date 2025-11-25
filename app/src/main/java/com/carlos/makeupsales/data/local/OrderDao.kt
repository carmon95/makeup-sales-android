package com.carlos.makeupsales.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

data class OrderWithCustomer(
    @Embedded val order: OrderEntity,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: CustomerEntity
)

@Dao
interface OrderDao {

    @Transaction
    @Query("SELECT * FROM orders ORDER BY dateMillis DESC")
    fun getAllOrdersWithCustomer(): Flow<List<OrderWithCustomer>>

    // Insert single order
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    // Insert multiple items (order_items table)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    // Update product stock by subtracting qty
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :productId")
    suspend fun decreaseProductStock(productId: Long, qty: Int)

    @Query("SELECT COUNT(*) FROM orders WHERE status = :status")
    suspend fun countByStatus(status: OrderStatus): Int

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItemsByOrderId(orderId: Long)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    // Transacción que borra items y luego la orden (atomicidad)
    @Transaction
    suspend fun deleteOrderWithItems(order: OrderEntity) {
        deleteOrderItemsByOrderId(order.id)
        deleteOrder(order)
    }

    // This method runs in a single DB transaction:
    @Transaction
    suspend fun insertOrderWithItemsAndUpdateStock(
        order: OrderEntity,
        items: List<OrderItemEntity>
    ): Long {
        // Insert order and get generated id
        val createdId = insertOrder(order)

        // Prepare items with the generated orderId
        val itemsToInsert = items.map { it.copy(orderId = createdId) }

        // Insert items
        insertOrderItems(itemsToInsert)

        // Update stock for each item
        itemsToInsert.forEach { item ->
            decreaseProductStock(item.productId, item.quantity)
        }

        return createdId
    }

    @Update
    suspend fun updateOrder(order: OrderEntity)

    /**
     * Añadido: obtener lista simple de OrderItemEntity por orderId.
     * Esto hace consistente el repositorio que llama a orderDao.getItemsByOrderId(...)
     */
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsByOrderId(orderId: Long): List<OrderItemEntity>
}
