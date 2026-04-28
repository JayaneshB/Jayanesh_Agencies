package com.chocowholesale.customer.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AddressDto(
    val id: String, val line1: String, val line2: String?,
    val city: String, val state: String, val pincode: String,
    val country: String, val isDefault: Boolean
)

@JsonClass(generateAdapter = true)
data class AddressBody(
    val line1: String, val line2: String?, val city: String,
    val state: String, val pincode: String, val country: String?,
    val isDefault: Boolean?
)
