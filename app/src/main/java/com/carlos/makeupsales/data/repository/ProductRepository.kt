package com.carlos.makeupsales.data.repository

import com.carlos.makeupsales.data.local.ProductDao
import com.carlos.makeupsales.data.local.ProductEntity
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {

    val products: Flow<List<ProductEntity>> = productDao.getAllProducts()

    suspend fun upsertProduct(
        name: String,
        brand: String?,
        category: String?,
        price: Double,
        stock: Int,
        imageUri: String?
    ) {
        val product = ProductEntity(
            name = name,
            brand = brand,
            category = category,
            price = price,
            stock = stock,
            imageUri = imageUri
        )
        productDao.upsertProduct(product)
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }
}
