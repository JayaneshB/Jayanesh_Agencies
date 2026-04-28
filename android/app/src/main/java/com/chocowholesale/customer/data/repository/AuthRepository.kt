package com.chocowholesale.customer.data.repository

import com.chocowholesale.customer.data.local.TokenManager
import com.chocowholesale.customer.data.remote.ApiService
import com.chocowholesale.customer.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) {
    val isLoggedIn = tokenManager.isLoggedIn
    val userName = tokenManager.userName

    suspend fun requestOtp(phone: String): Result<String> = runCatching {
        val resp = api.requestOtp(OtpRequestBody(phone))
        if (resp.isSuccessful) resp.body()!!.message
        else throw Exception("Failed to send OTP")
    }

    suspend fun verifyOtp(phone: String, code: String): Result<LoginResponseDto> = runCatching {
        val resp = api.verifyOtp(OtpVerifyBody(phone, code))
        if (resp.isSuccessful) {
            val body = resp.body()!!
            tokenManager.saveTokens(body.accessToken, body.refreshToken, body.name, phone)
            body
        } else throw Exception("Invalid OTP")
    }

    suspend fun updateProfile(name: String, businessName: String?, gstin: String?): Result<Unit> = runCatching {
        val resp = api.updateProfile(ProfileUpdateBody(name, businessName, gstin))
        if (resp.isSuccessful) tokenManager.updateName(name)
        else throw Exception("Update failed")
    }

    suspend fun logout() { tokenManager.clear() }
}
