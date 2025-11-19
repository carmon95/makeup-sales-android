package com.carlos.makeupsales.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long,
    val customerId: Long,
    val status: OrderStatus,
    val total: Double
)

enum class OrderStatus {
    PENDING,
    PAID,
    DELIVERED,
    CANCELED
}
