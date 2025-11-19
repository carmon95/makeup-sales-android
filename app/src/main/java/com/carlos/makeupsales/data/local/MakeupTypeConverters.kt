package com.carlos.makeupsales.data.local

import androidx.room.TypeConverter

class MakeupTypeConverters {

    @TypeConverter
    fun fromStatus(status: OrderStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): OrderStatus = OrderStatus.valueOf(value)
}
