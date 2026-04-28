package com.chocowholesale.customer.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CategoryDto(val id: String, val name: String, val description: String)

@JsonClass(generateAdapter = true)
data class ProductDto(
    val id: String,
    val name: String,
    val description: String?,
    val categoryId: String?,
    val categoryName: String?,
    val taxRate: Double?,
    val isActive: Boolean?,
    val inventory: InventoryDto?,
    val pricingTiers: List<PricingTierDto>?,
    val images: List<ProductImageDto>?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class InventoryDto(
    val totalStock: Int, val reserved: Int, val available: Int,
    val reorderThreshold: Int, val moq: Int
)

@JsonClass(generateAdapter = true)
data class PricingTierDto(val id: String?, val minQty: Int, val maxQty: Int?, val price: Double)

@JsonClass(generateAdapter = true)
data class ProductImageDto(val id: String?, val url: String, val position: Int?)

@JsonClass(generateAdapter = true)
data class PagedResponse<T>(
    val content: List<T>,
    val totalPages: Int,
    val totalElements: Long,
    val number: Int,
    val size: Int,
    val first: Boolean,
    val last: Boolean,
    val empty: Boolean
)
