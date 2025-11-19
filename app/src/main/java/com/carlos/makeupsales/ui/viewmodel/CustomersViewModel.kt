package com.carlos.makeupsales.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carlos.makeupsales.data.local.CustomerEntity
import com.carlos.makeupsales.data.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CustomersUiState(
    val customers: List<CustomerEntity> = emptyList(),
    val isLoading: Boolean = false
)

class CustomersViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomersUiState(isLoading = true))
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.customers.collectLatest { list ->
                _uiState.value = CustomersUiState(
                    customers = list,
                    isLoading = false
                )
            }
        }
    }

    fun saveCustomer(
        name: String,
        phone: String?,
        insta: String?,
        address: String?
    ) {
        viewModelScope.launch {
            repository.upsertCustomer(name, phone, insta, address)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    class Factory(
        private val repository: CustomerRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
                return CustomersViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
