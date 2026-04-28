package com.chocowholesale.customer.data.repository

import com.chocowholesale.customer.data.local.db.CachedCartItem
import com.chocowholesale.customer.data.local.db.CartDao
import com.chocowholesale.customer.data.remote.ApiService
import com.chocowholesale.customer.data.remote.dto.CartItemBody
import com.chocowholesale.customer.data.remote.dto.CartResponseDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val api: ApiService,
    private val cartDao: CartDao
) {
    val localCart = cartDao.getAll()
    val cartCount = cartDao.itemCount()

    suspend fun fetchCart(): Result<CartResponseDto> = runCatching {
        val resp = api.getCart()
        if (resp.isSuccessful) {
            val cart = resp.body()!!
            syncLocalFromRemote(cart)
            cart
        } else throw Exception("Failed to load cart")
    }

    suspend fun addToCart(productId: String, qty: Int): Result<CartResponseDto> = runCatching {
        val resp = api.addToCart(CartItemBody(productId, qty))
        if (resp.isSuccessful) {
            val cart = resp.body()!!
            syncLocalFromRemote(cart)
            cart
        } else throw Exception("Failed to add to cart")
    }

    suspend fun removeFromCart(productId: String): Result<CartResponseDto> = runCatching {
        val resp = api.removeFromCart(productId)
        if (resp.isSuccessful) {
            cartDao.delete(productId)
            resp.body()!!
        } else throw Exception("Failed to remove from cart")
    }

    suspend fun clearLocal() = cartDao.clearAll()

    private suspend fun syncLocalFromRemote(cart: CartResponseDto) {
        cartDao.clearAll()
        cart.items.forEach {
            cartDao.upsert(CachedCartItem(
                productId = it.productId, productName = it.productName,
                imageUrl = it.imageUrl, quantity = it.quantity,
                unitPrice = it.unitPrice, availableStock = it.availableStock
            ))
        }
    }
}
