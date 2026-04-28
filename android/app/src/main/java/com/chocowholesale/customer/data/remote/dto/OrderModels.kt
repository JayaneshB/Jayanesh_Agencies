package com.chocowholesale.customer.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlaceOrderBody(val addressId: String)

@JsonClass(generateAdapter = true)
data class VerifyPaymentBody(val paymentId: String, val signature: String)

@JsonClass(generateAdapter = true)
data class OrderDto(
    val id: String,
    val status: String,
    val paymentStatus: String,
    val paymentId: String?,
    val subtotal: Double,
    val taxAmount: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val items: List<OrderItemDto>?,
    val statusHistory: List<StatusHistoryDto>?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class OrderItemDto(
    val id: String, val productId: String, val productName: String,
    val quantity: Int, val unitPrice: Double, val totalPrice: Double
)

@JsonClass(generateAdapter = true)
data class StatusHistoryDto(
    val fromStatus: String?, val toStatus: String,
    val reason: String?, val createdAt: String?
)
