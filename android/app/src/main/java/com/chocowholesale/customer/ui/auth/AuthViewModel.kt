package com.chocowholesale.customer.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chocowholesale.customer.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false,
    val verified: Boolean = false,
    val isNewUser: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val isLoggedIn = authRepo.isLoggedIn

    fun requestOtp(phone: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepo.requestOtp(phone)
                .onSuccess { _state.update { it.copy(isLoading = false, otpSent = true) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepo.verifyOtp(phone, code)
                .onSuccess { resp ->
                    _state.update {
                        it.copy(isLoading = false, verified = true, isNewUser = resp.isNewUser == true)
                    }
                }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun updateProfile(name: String, businessName: String?, gstin: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepo.updateProfile(name, businessName, gstin)
                .onSuccess { _state.update { it.copy(isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
}
