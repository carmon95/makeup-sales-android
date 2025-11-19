package com.carlos.makeupsales.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String?,
    val category: String?,
    val price: Double,
    val stock: Int,
    // NUEVO: Uri de la imagen (puede ser null si no se eligió)
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null
)
