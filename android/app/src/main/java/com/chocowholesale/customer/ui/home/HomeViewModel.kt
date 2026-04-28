package com.chocowholesale.customer.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chocowholesale.customer.data.remote.dto.CategoryDto
import com.chocowholesale.customer.data.remote.dto.ProductDto
import com.chocowholesale.customer.data.repository.CatalogRepository
import com.chocowholesale.customer.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val categories: List<CategoryDto> = emptyList(),
    val products: List<ProductDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val cartQuantities: Map<String, Int> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val catalogRepo: CatalogRepository,
    private val cartRepo: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    val cartCount = cartRepo.cartCount

    init {
        load()
        viewModelScope.launch {
            cartRepo.localCart.collect { items ->
                _state.update { it.copy(cartQuantities = items.associate { ci -> ci.productId to ci.quantity }) }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val cats = catalogRepo.getCategories()
            val prods = catalogRepo.getProducts(page = 0, size = 20)
            _state.update {
                it.copy(
                    isLoading = false,
                    categories = cats.getOrDefault(emptyList()),
                    products = prods.getOrNull()?.content ?: emptyList(),
                    error = cats.exceptionOrNull()?.message ?: prods.exceptionOrNull()?.message
                )
            }
        }
    }

    fun addToCart(productId: String, qty: Int = 1) {
        viewModelScope.launch { cartRepo.addToCart(productId, qty) }
    }

    fun removeFromCart(productId: String) {
        viewModelScope.launch { cartRepo.removeFromCart(productId) }
    }
}
