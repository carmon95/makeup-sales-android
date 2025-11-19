package com.carlos.makeupsales

import android.app.Application
import com.carlos.makeupsales.data.local.MakeupDatabase
import com.carlos.makeupsales.data.repository.CustomerRepository
import com.carlos.makeupsales.data.repository.OrderRepository
import com.carlos.makeupsales.data.repository.ProductRepository

class MakeupApplication : Application() {

    val database by lazy { MakeupDatabase.getInstance(this) }

    val productRepository by lazy { ProductRepository(database.productDao()) }
    val customerRepository by lazy { CustomerRepository(database.customerDao()) }
    val orderRepository by lazy { OrderRepository(database.orderDao()) }
}
