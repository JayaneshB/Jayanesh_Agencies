package com.chocowholesale.customer.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CachedCartItem::class, CachedProduct::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun productDao(): ProductDao
}
