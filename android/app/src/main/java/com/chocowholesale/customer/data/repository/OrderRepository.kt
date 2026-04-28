package com.chocowholesale.customer.data.repository

import com.chocowholesale.customer.data.remote.ApiService
import com.chocowholesale.customer.data.remote.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(private val api: ApiService) {

    suspend fun placeOrder(addressId: String): Result<OrderDto> = runCatching {
        val resp = api.placeOrder(PlaceOrderBody(addressId))
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Failed to place order")
    }

    suspend fun getOrders(page: Int = 0): Result<PagedResponse<OrderDto>> = runCatching {
        val resp = api.getOrders(page)
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Failed to load orders")
    }

    suspend fun getOrderDetail(id: String): Result<OrderDto> = runCatching {
        val resp = api.getOrderDetail(id)
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Order not found")
    }

    suspend fun verifyPayment(orderId: String, paymentId: String, signature: String): Result<OrderDto> = runCatching {
        val resp = api.verifyPayment(orderId, VerifyPaymentBody(paymentId, signature))
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Payment verification failed")
    }
}
