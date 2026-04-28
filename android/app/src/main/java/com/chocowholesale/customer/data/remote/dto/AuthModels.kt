package com.chocowholesale.customer.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpRequestBody(val phone: String)

@JsonClass(generateAdapter = true)
data class OtpVerifyBody(val phone: String, val code: String)

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val role: String,
    val name: String,
    val isNewUser: Boolean?
)

@JsonClass(generateAdapter = true)
data class ProfileUpdateBody(val name: String?, val businessName: String?, val gstin: String?)

@JsonClass(generateAdapter = true)
data class ProfileDto(
    val id: String,
    val phone: String,
    val name: String,
    val businessName: String,
    val gstin: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class MessageResponse(val message: String)
