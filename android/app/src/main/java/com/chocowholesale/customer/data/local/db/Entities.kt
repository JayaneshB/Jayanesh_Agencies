package com.chocowholesale.customer.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CachedCartItem(
    @PrimaryKey val productId: String,
    val productName: String,
    val imageUrl: String?,
    val quantity: Int,
    val unitPrice: Double,
    val availableStock: Int
)

@Entity(tableName = "cached_products")
data class CachedProduct(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val categoryId: String?,
    val categoryName: String?,
    val imageUrl: String?,
    val minPrice: Double?,
    val available: Int?,
    val moq: Int?
)
