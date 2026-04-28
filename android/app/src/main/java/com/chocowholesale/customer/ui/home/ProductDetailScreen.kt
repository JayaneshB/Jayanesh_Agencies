package com.chocowholesale.customer.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.chocowholesale.customer.data.remote.dto.ProductDto
import com.chocowholesale.customer.data.repository.CartRepository
import com.chocowholesale.customer.data.repository.CatalogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val catalogRepo: CatalogRepository,
    private val cartRepo: CartRepository
) : ViewModel() {
    private val _product = MutableStateFlow<ProductDto?>(null)
    val product = _product.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()
    private val _added = MutableStateFlow(false)
    val added = _added.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _loading.value = true
            catalogRepo.getProductDetail(id).onSuccess { _product.value = it }
            _loading.value = false
        }
    }

    fun addToCart(productId: String, qty: Int) {
        viewModelScope.launch {
            cartRepo.addToCart(productId, qty).onSuccess { _added.value = true }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val product by viewModel.product.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val added by viewModel.added.collectAsState()
    var qty by remember { mutableIntStateOf(1) }

    LaunchedEffect(productId) { viewModel.load(productId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(product?.name ?: "Product") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            }
        )
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else product?.let { p ->
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                val imageUrl = p.images?.firstOrNull()?.url
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl, contentDescription = p.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(240.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }

                Text(p.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                p.categoryName?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Spacer(Modifier.height(8.dp))
                p.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

                Spacer(Modifier.height(16.dp))
                Text("Pricing Tiers", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(4.dp))
                p.pricingTiers?.sortedBy { it.minQty }?.forEach { tier ->
                    val range = if (tier.maxQty != null) "${tier.minQty}–${tier.maxQty} units"
                        else "${tier.minQty}+ units"
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(range, style = MaterialTheme.typography.bodyMedium)
                        Text("₹${tier.price}", fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(Modifier.height(16.dp))
                val stock = p.inventory?.available ?: 0
                val moq = p.inventory?.moq ?: 1
                Text("Available: $stock | MOQ: $moq",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { if (qty > moq) qty-- }) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    Text("$qty", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { if (qty < stock) qty++ }) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.addToCart(p.id, qty) },
                    enabled = stock > 0 && !added,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(if (added) "Added ✓" else "Add to Cart", fontSize = 16.sp)
                }
            }
        }
    }
}
