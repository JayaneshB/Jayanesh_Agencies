package com.chocowholesale.customer.data.repository

import com.chocowholesale.customer.data.local.db.CachedProduct
import com.chocowholesale.customer.data.local.db.ProductDao
import com.chocowholesale.customer.data.remote.ApiService
import com.chocowholesale.customer.data.remote.dto.CategoryDto
import com.chocowholesale.customer.data.remote.dto.PagedResponse
import com.chocowholesale.customer.data.remote.dto.ProductDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val api: ApiService,
    private val productDao: ProductDao
) {
    suspend fun getCategories(): Result<List<CategoryDto>> = runCatching {
        val resp = api.getCategories()
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Failed to load categories")
    }

    suspend fun getProducts(
        page: Int = 0, size: Int = 20,
        search: String? = null, categoryId: String? = null
    ): Result<PagedResponse<ProductDto>> = runCatching {
        val resp = api.getProducts(page, size, search, categoryId)
        if (resp.isSuccessful) {
            val data = resp.body()!!
            // Cache locally
            productDao.insertAll(data.content.map { it.toCached() })
            data
        } else throw Exception("Failed to load products")
    }

    suspend fun getProductDetail(id: String): Result<ProductDto> = runCatching {
        val resp = api.getProductDetail(id)
        if (resp.isSuccessful) resp.body()!!
        else throw Exception("Product not found")
    }

    fun cachedProducts() = productDao.getAll()

    private fun ProductDto.toCached() = CachedProduct(
        id = id, name = name, description = description,
        categoryId = categoryId, categoryName = categoryName,
        imageUrl = images?.firstOrNull()?.url,
        minPrice = pricingTiers?.minByOrNull { it.minQty }?.price,
        available = inventory?.available,
        moq = inventory?.moq
    )
}
