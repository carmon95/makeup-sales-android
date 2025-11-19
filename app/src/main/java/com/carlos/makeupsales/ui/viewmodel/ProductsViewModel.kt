package com.carlos.makeupsales.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.carlos.makeupsales.data.local.ProductEntity
import com.carlos.makeupsales.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<ProductEntity> = emptyList(),
    val isLoading: Boolean = false
)

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState(isLoading = true))
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.products.collectLatest { list ->
                _uiState.value = ProductsUiState(
                    products = list,
                    isLoading = false
                )
            }
        }
    }

    fun saveProduct(
        name: String,
        brand: String?,
        category: String?,
        price: Double,
        stock: Int,
        imageUri: String?
    ) {
        viewModelScope.launch {
            repository.upsertProduct(
                name = name,
                brand = brand,
                category = category,
                price = price,
                stock = stock,
                imageUri = imageUri
            )
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    class Factory(
        private val repository: ProductRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
                return ProductsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
