package com.chocowholesale.customer.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chocowholesale.customer.data.remote.dto.ProductDto
import com.chocowholesale.customer.data.repository.CartRepository
import com.chocowholesale.customer.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val catalogRepo: CatalogRepository,
    private val cartRepo: CartRepository
) : ViewModel() {
    private val _results = MutableStateFlow<List<ProductDto>>(emptyList())
    val results = _results.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        if (query.length < 2) { _results.value = emptyList(); return }
        searchJob = viewModelScope.launch {
            delay(500)
            _loading.value = true
            catalogRepo.getProducts(search = query).onSuccess { _results.value = it.content }
            _loading.value = false
        }
    }

    fun addToCart(productId: String) {
        viewModelScope.launch { cartRepo.addToCart(productId, 1) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onProductClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val results by viewModel.results.collectAsState()
    val loading by viewModel.loading.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Search Products") })
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.search(it) },
            placeholder = { Text("Search chocolates, sweets...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { product ->
                ProductCard(product, onClick = { onProductClick(product.id) },
                    onAddToCart = { viewModel.addToCart(product.id) })
            }
        }
    }
}
