package com.chocowholesale.customer.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CartItemBody(val productId: String, val quantity: Int)

@JsonClass(generateAdapter = true)
data class CartResponseDto(
    val items: List<CartItemResponseDto>,
    val subtotal: Double,
    val totalItems: Int
)

@JsonClass(generateAdapter = true)
data class CartItemResponseDto(
    val productId: String,
    val productName: String,
    val imageUrl: String?,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double,
    val availableStock: Int
)
