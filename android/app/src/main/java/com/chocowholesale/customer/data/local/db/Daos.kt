package com.chocowholesale.customer.data.local.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAll(): Flow<List<CachedCartItem>>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun itemCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CachedCartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun delete(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM cached_products")
    fun getAll(): Flow<List<CachedProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<CachedProduct>)

    @Query("DELETE FROM cached_products")
    suspend fun clearAll()
}
