package com.chocowholesale.customer.data.repository

import com.chocowholesale.customer.data.remote.ApiService
import com.chocowholesale.customer.data.remote.dto.AddressBody
import com.chocowholesale.customer.data.remote.dto.AddressDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(private val api: ApiService) {

    suspend fun getAddresses(): Result<List<AddressDto>> = runCatching {
        val resp = api.getAddresses()
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Failed to load addresses")
    }

    suspend fun createAddress(body: AddressBody): Result<AddressDto> = runCatching {
        val resp = api.createAddress(body)
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Failed to create address")
    }

    suspend fun updateAddress(id: String, body: AddressBody): Result<AddressDto> = runCatching {
        val resp = api.updateAddress(id, body)
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Failed to update address")
    }

    suspend fun deleteAddress(id: String): Result<Unit> = runCatching {
        val resp = api.deleteAddress(id)
        if (!resp.isSuccessful) throw Exception("Failed to delete address")
    }
}
