package com.chocowholesale.customer.ui.cart

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.chocowholesale.customer.data.local.db.CachedCartItem
import com.chocowholesale.customer.data.remote.dto.AddressBody
import com.chocowholesale.customer.data.remote.dto.AddressDto
import com.chocowholesale.customer.data.remote.dto.OrderDto
import com.chocowholesale.customer.data.repository.AddressRepository
import com.chocowholesale.customer.data.repository.CartRepository
import com.chocowholesale.customer.data.repository.OrderRepository
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class CartUiState(
    val isLoading: Boolean = true,
    val addresses: List<AddressDto> = emptyList(),
    val selectedAddressId: String? = null,
    val pendingOrder: OrderDto? = null,
    val orderPlaced: Boolean = false,
    val error: String? = null,
    val showAddAddress: Boolean = false,
    val savingAddress: Boolean = false
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepo: CartRepository,
    private val addressRepo: AddressRepository,
    private val orderRepo: OrderRepository
) : ViewModel() {
    val cartItems = cartRepo.localCart
    private val _ui = MutableStateFlow(CartUiState())
    val ui = _ui.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            cartRepo.fetchCart()
            addressRepo.getAddresses().onSuccess { addrs ->
                _ui.update { it.copy(addresses = addrs,
                    selectedAddressId = addrs.firstOrNull { a -> a.isDefault }?.id ?: addrs.firstOrNull()?.id) }
            }
            _ui.update { it.copy(isLoading = false) }
        }
    }

    fun updateQty(productId: String, delta: Int) {
        viewModelScope.launch { cartRepo.addToCart(productId, delta) }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch { cartRepo.removeFromCart(productId) }
    }

    fun selectAddress(id: String) { _ui.update { it.copy(selectedAddressId = id) } }

    fun toggleAddAddress() { _ui.update { it.copy(showAddAddress = !it.showAddAddress) } }

    fun addAddress(body: AddressBody) {
        viewModelScope.launch {
            _ui.update { it.copy(savingAddress = true, error = null) }
            addressRepo.createAddress(body)
                .onSuccess { addr ->
                    _ui.update { it.copy(
                        savingAddress = false,
                        showAddAddress = false,
                        addresses = it.addresses + addr,
                        selectedAddressId = addr.id
                    ) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(savingAddress = false, error = "Failed to save address: ${e.message}") }
                }
        }
    }

    fun placeOrder() {
        val addressId = _ui.value.selectedAddressId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            orderRepo.placeOrder(addressId)
                .onSuccess { order ->
                    _ui.update { it.copy(isLoading = false, pendingOrder = order) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onPaymentSuccess(paymentId: String) {
        val order = _ui.value.pendingOrder ?: return
        viewModelScope.launch {
            orderRepo.verifyPayment(order.id, paymentId, "")
                .onSuccess {
                    cartRepo.clearLocal()
                    _ui.update { it.copy(pendingOrder = null, orderPlaced = true) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(error = "Payment received but verification failed: ${e.message}") }
                }
        }
    }

    fun onPaymentFailure(errorMessage: String) {
        _ui.update { it.copy(pendingOrder = null, error = "Payment failed: $errorMessage") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onOrderPlaced: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val items by viewModel.cartItems.collectAsState(initial = emptyList())
    val ui by viewModel.ui.collectAsState()
    val context = LocalContext.current

    // Address dialog fields
    var addrLine1 by remember { mutableStateOf("") }
    var addrCity by remember { mutableStateOf("") }
    var addrState by remember { mutableStateOf("") }
    var addrPincode by remember { mutableStateOf("") }

    LaunchedEffect(ui.orderPlaced) { if (ui.orderPlaced) onOrderPlaced() }

    // Clear address fields when address is saved successfully (form hidden)
    LaunchedEffect(ui.showAddAddress) {
        if (!ui.showAddAddress) {
            addrLine1 = ""; addrCity = ""; addrState = ""; addrPincode = ""
        }
    }

    // Register Razorpay callbacks with Activity
    DisposableEffect(context) {
        val activity = context as? com.chocowholesale.customer.MainActivity
        activity?.onPaymentSuccess = { paymentId -> viewModel.onPaymentSuccess(paymentId) }
        activity?.onPaymentFailure = { msg -> viewModel.onPaymentFailure(msg) }
        onDispose {
            activity?.onPaymentSuccess = null
            activity?.onPaymentFailure = null
        }
    }

    // Launch Razorpay when we have a pending order
    LaunchedEffect(ui.pendingOrder) {
        val order = ui.pendingOrder ?: return@LaunchedEffect
        val activity = context as? Activity ?: return@LaunchedEffect
        try {
            val checkout = Checkout()
            checkout.setKeyID("rzp_test_REPLACE_ME")
            val options = JSONObject().apply {
                put("name", "Choco Wholesale")
                put("description", "Order #${order.id.take(8)}")
                put("currency", "INR")
                put("amount", (order.totalAmount * 100).toLong())
                put("prefill", JSONObject().apply {
                    put("contact", "")
                })
                put("theme", JSONObject().apply { put("color", "#4F46E5") })
            }
            checkout.open(activity, options)
        } catch (e: Exception) {
            viewModel.onPaymentFailure(e.message ?: "Could not open Razorpay")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Cart") })

        if (ui.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty", style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items, key = { it.productId }) { item ->
                    CartItemRow(item, onQtyChange = { viewModel.updateQty(item.productId, it) },
                        onRemove = { viewModel.removeItem(item.productId) })
                }
            }

            // Address section
            Column(modifier = Modifier.padding(16.dp, 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Delivery Address", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    TextButton(onClick = { viewModel.toggleAddAddress() }) {
                        Text(if (ui.showAddAddress) "Cancel" else "+ Add New")
                    }
                }

                if (ui.showAddAddress) {
                    OutlinedTextField(value = addrLine1, onValueChange = { addrLine1 = it },
                        label = { Text("Address Line") }, modifier = Modifier.fillMaxWidth(),
                        singleLine = true)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = addrCity, onValueChange = { addrCity = it },
                            label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = addrState, onValueChange = { addrState = it },
                            label = { Text("State") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = addrPincode, onValueChange = { addrPincode = it },
                            label = { Text("Pincode") }, modifier = Modifier.weight(1f), singleLine = true)
                        Button(onClick = {
                            viewModel.addAddress(AddressBody(addrLine1, null, addrCity, addrState,
                                addrPincode, "India", true))
                        }, enabled = addrLine1.isNotBlank() && addrCity.isNotBlank() && addrPincode.isNotBlank() && !ui.savingAddress,
                            modifier = Modifier.weight(1f).padding(top = 8.dp)) {
                            if (ui.savingAddress) CircularProgressIndicator(
                                modifier = Modifier.size(16.dp), strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            else Text("Save")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (ui.addresses.isNotEmpty()) {
                    ui.addresses.forEach { addr ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()) {
                            RadioButton(selected = addr.id == ui.selectedAddressId,
                                onClick = { viewModel.selectAddress(addr.id) })
                            Text("${addr.line1}, ${addr.city} - ${addr.pincode}",
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else if (!ui.showAddAddress) {
                    Text("No address saved. Add one to place an order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error)
                }
            }

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp, 0.dp),
                    style = MaterialTheme.typography.bodySmall)
            }

            // Totals and place order
            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            Surface(tonalElevation = 2.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", fontWeight = FontWeight.Medium)
                        Text("₹${"%.2f".format(subtotal)}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.placeOrder() },
                        enabled = ui.selectedAddressId != null && ui.pendingOrder == null,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Text("Pay & Place Order", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(item: CachedCartItem, onQtyChange: (Int) -> Unit, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (item.imageUrl != null) {
                AsyncImage(model = item.imageUrl, contentDescription = null,
                    modifier = Modifier.size(56.dp))
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium)
                Text("₹${item.unitPrice} × ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQtyChange(-1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, "Less", modifier = Modifier.size(16.dp))
                }
                Text("${item.quantity}", fontWeight = FontWeight.Bold)
                IconButton(onClick = { onQtyChange(1) }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, "More", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
