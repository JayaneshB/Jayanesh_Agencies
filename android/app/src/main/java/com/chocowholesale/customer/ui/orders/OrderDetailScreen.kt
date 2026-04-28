package com.chocowholesale.customer.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
class OrderDetailViewModel @Inject constructor(
    private val orderRepo: OrderRepository
) : ViewModel() {
    private val _order = MutableStateFlow<OrderDto?>(null)
    val order = _order.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            _loading.value = true
            orderRepo.getOrderDetail(id).onSuccess { _order.value = it }
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val order by viewModel.order.collectAsState()
    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(orderId) { viewModel.load(orderId) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Order Details") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }
        )
        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else order?.let { o ->
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Status", style = MaterialTheme.typography.bodySmall)
                            Text(o.status.replace("_", " "), fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Subtotal", "₹${"%.2f".format(o.subtotal)}")
                        InfoRow("Tax", "₹${"%.2f".format(o.taxAmount)}")
                        InfoRow("Delivery", "₹${"%.2f".format(o.deliveryFee)}")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Total", "₹${"%.2f".format(o.totalAmount)}", bold = true)
                        Spacer(Modifier.height(4.dp))
                        InfoRow("Payment", o.paymentStatus)
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Items", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                o.items?.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(item.productName, fontWeight = FontWeight.Medium)
                                Text("${item.quantity} × ₹${"%.2f".format(item.unitPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                            Text("₹${"%.2f".format(item.totalPrice)}",
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!o.statusHistory.isNullOrEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Status Timeline", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    o.statusHistory.forEach { h ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("→ ", color = MaterialTheme.colorScheme.primary)
                            Text(h.toStatus.replace("_", " "),
                                style = MaterialTheme.typography.bodySmall)
                            h.createdAt?.take(16)?.let {
                                Spacer(Modifier.width(8.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium)
    }
}
