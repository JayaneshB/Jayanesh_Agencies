package com.chocowholesale.customer.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chocowholesale.customer.data.remote.dto.ProductDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCategoryClick: (String, String) -> Unit,
    onProductClick: (String) -> Unit,
    onGoToCart: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState(initial = 0)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Choco Wholesale", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Categories horizontal row
                    if (state.categories.isNotEmpty()) {
                        Text("Categories", fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.categories) { cat ->
                                AssistChip(
                                    onClick = { onCategoryClick(cat.id, cat.name) },
                                    label = { Text(cat.name) }
                                )
                            }
                        }
                    }

                    Text("All Products", fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp,
                            top = 0.dp, bottom = if (cartCount > 0) 72.dp else 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.products) { product ->
                            val qtyInCart = state.cartQuantities[product.id] ?: 0
                            ProductCard(
                                product = product,
                                qtyInCart = qtyInCart,
                                onClick = { onProductClick(product.id) },
                                onAddToCart = { viewModel.addToCart(product.id) },
                                onIncrement = { viewModel.addToCart(product.id, 1) },
                                onDecrement = {
                                    if (qtyInCart <= 1) viewModel.removeFromCart(product.id)
                                    else viewModel.addToCart(product.id, -1)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating "Go to Cart" bar
        if (cartCount > 0) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth().padding(12.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp,
                onClick = onGoToCart
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("$cartCount item${if (cartCount > 1) "s" else ""} in cart",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                    Text("Go to Cart →", color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductDto,
    qtyInCart: Int = 0,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onIncrement: () -> Unit = onAddToCart,
    onDecrement: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            val imageUrl = product.images?.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl, contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center) {
                    Text("No Image", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(product.name, fontWeight = FontWeight.Medium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium)
                product.categoryName?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                val minPrice = product.pricingTiers?.minByOrNull { it.minQty }?.price
                if (minPrice != null) {
                    Text("₹${minPrice}", fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                val stock = product.inventory?.available ?: 0
                if (stock <= 0) {
                    Text("Out of stock", color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                } else {
                    Spacer(Modifier.height(4.dp))
                    if (qtyInCart > 0) {
                        // Quantity counter
                        Row(
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilledIconButton(
                                onClick = onDecrement,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(14.dp))
                            }
                            Text("$qtyInCart", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            FilledIconButton(
                                onClick = onIncrement,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        Button(
                            onClick = onAddToCart,
                            modifier = Modifier.fillMaxWidth().height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Add to Cart", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
