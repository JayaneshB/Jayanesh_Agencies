package com.chocowholesale.customer.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chocowholesale.customer.data.remote.dto.ProductDto
import com.chocowholesale.customer.data.repository.CartRepository
import com.chocowholesale.customer.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val catalogRepo: CatalogRepository,
    private val cartRepo: CartRepository
) : ViewModel() {
    private val _products = MutableStateFlow<List<ProductDto>>(emptyList())
    val products = _products.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    fun loadCategory(categoryId: String) {
        viewModelScope.launch {
            _loading.value = true
            catalogRepo.getProducts(categoryId = categoryId).onSuccess {
                _products.value = it.content
            }
            _loading.value = false
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch { cartRepo.addToCart(productId, 1) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoryId: String,
    categoryName: String,
    onProductClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(categoryId) { viewModel.loadCategory(categoryId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(categoryName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductCard(product, onClick = { onProductClick(product.id) },
                        onAddToCart = { viewModel.addToCart(product.id) })
                }
            }
        }
    }
}
