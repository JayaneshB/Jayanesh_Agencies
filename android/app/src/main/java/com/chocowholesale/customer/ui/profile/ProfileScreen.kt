package com.chocowholesale.customer.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
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
import com.chocowholesale.customer.data.local.TokenManager
import com.chocowholesale.customer.data.remote.dto.AddressBody
import com.chocowholesale.customer.data.remote.dto.AddressDto
import com.chocowholesale.customer.data.repository.AddressRepository
import com.chocowholesale.customer.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val addressRepo: AddressRepository,
    private val tokenManager: TokenManager
) : ViewModel() {
    val userName = tokenManager.userName
    val userPhone = tokenManager.userPhone
    private val _addresses = MutableStateFlow<List<AddressDto>>(emptyList())
    val addresses = _addresses.asStateFlow()
    private val _loggedOut = MutableStateFlow(false)
    val loggedOut = _loggedOut.asStateFlow()

    init { loadAddresses() }

    fun loadAddresses() {
        viewModelScope.launch {
            addressRepo.getAddresses().onSuccess { _addresses.value = it }
        }
    }

    fun addAddress(body: AddressBody) {
        viewModelScope.launch {
            addressRepo.createAddress(body).onSuccess { loadAddresses() }
        }
    }

    fun deleteAddress(id: String) {
        viewModelScope.launch {
            addressRepo.deleteAddress(id).onSuccess { loadAddresses() }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _loggedOut.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val name by viewModel.userName.collectAsState(initial = "")
    val phone by viewModel.userPhone.collectAsState(initial = "")
    val addresses by viewModel.addresses.collectAsState()
    val loggedOut by viewModel.loggedOut.collectAsState()

    // Address dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var addrLine1 by remember { mutableStateOf("") }
    var addrCity by remember { mutableStateOf("") }
    var addrState by remember { mutableStateOf("") }
    var addrPincode by remember { mutableStateOf("") }

    if (loggedOut) {
        // Force restart — simplest approach
        LaunchedEffect(Unit) {
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Profile") },
            actions = {
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.AutoMirrored.Filled.Logout, "Logout")
                }
            }
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(name ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("+91 ${phone ?: ""}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Saved Addresses", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                TextButton(onClick = { showAddDialog = true }) { Text("+ Add") }
            }
            Spacer(Modifier.height(8.dp))
            if (addresses.isEmpty()) {
                Text("No saved addresses", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            } else {
                addresses.forEach { addr ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${addr.line1}, ${addr.city}", fontWeight = FontWeight.Medium)
                                Text("${addr.state} - ${addr.pincode}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                if (addr.isDefault) {
                                    Text("Default", color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteAddress(addr.id) }) {
                                Icon(Icons.Default.Delete, "Delete",
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Contact Support", fontWeight = FontWeight.Medium)
                        Text("WhatsApp us for any queries",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Address") },
            text = {
                Column {
                    OutlinedTextField(value = addrLine1, onValueChange = { addrLine1 = it },
                        label = { Text("Address Line") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = addrCity, onValueChange = { addrCity = it },
                        label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = addrState, onValueChange = { addrState = it },
                        label = { Text("State") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = addrPincode, onValueChange = { addrPincode = it },
                        label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addAddress(AddressBody(addrLine1, null, addrCity, addrState,
                        addrPincode, "India", true))
                    showAddDialog = false
                    addrLine1 = ""; addrCity = ""; addrState = ""; addrPincode = ""
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}
