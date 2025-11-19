package com.carlos.makeupsales.data.repository

import com.carlos.makeupsales.data.local.CustomerDao
import com.carlos.makeupsales.data.local.CustomerEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(
    private val customerDao: CustomerDao
) {
    val customers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun upsertCustomer(
        name: String,
        phone: String?,
        instagramOrWhatsapp: String?,
        address: String?
    ): Long {
        val customer = CustomerEntity(
            name = name,
            phone = phone,
            instagramOrWhatsapp = instagramOrWhatsapp,
            address = address
        )
        return customerDao.upsertCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }
}
