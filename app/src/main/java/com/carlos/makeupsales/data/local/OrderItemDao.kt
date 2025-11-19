package com.carlos.makeupsales.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class OrderItemWithProduct(
    @Embedded val item: OrderItemEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity
)

@Dao
interface OrderItemDao {

    /**
     * Obtiene items de una orden, incluyendo información del producto.
     * (Flow -> útil si quieres UI reactiva con nombre del producto).
     */
    @Transaction
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrder(orderId: Long): Flow<List<OrderItemWithProduct>>

    /**
     * Obtiene lista simple de OrderItemEntity (sin producto).
     * Este SÍ es el que usa OrderRepository.getItemsForOrder(orderId).
     */
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getItemsByOrderId(orderId: Long): List<OrderItemEntity>

    /**
     * Inserta múltiples items.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<OrderItemEntity>)

    /**
     * Borra todos los items de una orden.
     */
    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)
}
