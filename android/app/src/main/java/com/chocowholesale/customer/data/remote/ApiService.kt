package com.chocowholesale.customer.data.remote

import com.chocowholesale.customer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/otp/request")
    suspend fun requestOtp(@Body body: OtpRequestBody): Response<MessageResponse>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: OtpVerifyBody): Response<LoginResponseDto>

    @GET("auth/me")
    suspend fun getProfile(): Response<ProfileDto>

    @PUT("auth/me")
    suspend fun updateProfile(@Body body: ProfileUpdateBody): Response<MessageResponse>

    // Catalog (public)
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null,
        @Query("categoryId") categoryId: String? = null
    ): Response<PagedResponse<ProductDto>>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: String): Response<ProductDto>

    // Cart
    @GET("cart")
    suspend fun getCart(): Response<CartResponseDto>

    @POST("cart/items")
    suspend fun addToCart(@Body item: CartItemBody): Response<CartResponseDto>

    @DELETE("cart/items/{productId}")
    suspend fun removeFromCart(@Path("productId") productId: String): Response<CartResponseDto>

    @PUT("cart")
    suspend fun syncCart(@Body items: List<CartItemBody>): Response<CartResponseDto>

    // Orders
    @POST("orders")
    suspend fun placeOrder(@Body body: PlaceOrderBody): Response<OrderDto>

    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<PagedResponse<OrderDto>>

    @GET("orders/{id}")
    suspend fun getOrderDetail(@Path("id") id: String): Response<OrderDto>

    @POST("orders/{id}/verify-payment")
    suspend fun verifyPayment(@Path("id") id: String, @Body body: VerifyPaymentBody): Response<OrderDto>

    // Addresses
    @GET("addresses")
    suspend fun getAddresses(): Response<List<AddressDto>>

    @POST("addresses")
    suspend fun createAddress(@Body body: AddressBody): Response<AddressDto>

    @PUT("addresses/{id}")
    suspend fun updateAddress(@Path("id") id: String, @Body body: AddressBody): Response<AddressDto>

    @DELETE("addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: String): Response<Unit>

    // Notifications
    @POST("notifications/token")
    suspend fun registerFcmToken(@Body body: Map<String, String>): Response<MessageResponse>
}
