package com.chocowholesale.customer.ui.orders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chocowholesale.customer.data.remote.dto.OrderDto
import com.chocowholesale.customer.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val orderRepo: OrderRepository
) : ViewModel() {
    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders = _orders.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            orderRepo.getOrders().onSuccess { _orders.value = it.content }
            _loading.value = false
        }
    }
}

private val statusColors = mapOf(
    "PENDING_PAYMENT" to androidx.compose.ui.graphics.Color(0xFFF59E0B),
    "CONFIRMED" to androidx.compose.ui.graphics.Color(0xFF3B82F6),
    "PROCESSING" to androidx.compose.ui.graphics.Color(0xFF6366F1),
    "PACKED" to androidx.compose.ui.graphics.Color(0xFF8B5CF6),
    "SHIPPED" to androidx.compose.ui.graphics.Color(0xFF06B6D4),
    "OUT_FOR_DELIVERY" to androidx.compose.ui.graphics.Color(0xFFF97316),
    "DELIVERED" to androidx.compose.ui.graphics.Color(0xFF22C55E),
    "CANCELLED" to androidx.compose.ui.graphics.Color(0xFFEF4444)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    onOrderClick: (String) -> Unit,
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()
    val loading by viewModel.loading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("My Orders") })
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (orders.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders) { order ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOrderClick(order.id) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("#${order.id.take(8)}…", fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium)
                                Text(order.status.replace("_", " "),
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColors[order.status] ?: MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("₹${"%.2f".format(order.totalAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary)
                                order.createdAt?.take(10)?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
