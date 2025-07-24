package io.yavero.almasasuite.pos.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime


interface LocalDatabase {


    suspend fun insertProducts(products: List<LocalProduct>)
    suspend fun getAllProducts(): List<LocalProduct>
    suspend fun getProductBySku(sku: String): LocalProduct?
    suspend fun updateProductStock(sku: String, newQuantity: Int)
    suspend fun getProductsFlow(): Flow<List<LocalProduct>>


    suspend fun insertUsers(users: List<LocalUser>)
    suspend fun getAllUsers(): List<LocalUser>
    suspend fun getUserByPin(pinHash: String): LocalUser?


    suspend fun insertSale(sale: LocalSale): Long
    suspend fun insertSaleItems(saleItems: List<LocalSaleItem>)
    suspend fun getUnsyncedSales(): List<LocalSaleWithItems>
    suspend fun markSaleAsSynced(saleId: Long)
    suspend fun getSalesForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<LocalSaleWithItems>


    suspend fun getLastSyncTimestamp(): Long?
    suspend fun updateLastSyncTimestamp(timestamp: Long)
    suspend fun clearAllData()


    fun isOnline(): Boolean
    fun setOnlineStatus(isOnline: Boolean)
    fun getOnlineStatusFlow(): Flow<Boolean>
}


data class LocalProduct(
    val id: String,
    val sku: String,
    val imageUrl: String?,
    val type: String,
    val karat: Int,
    val weightGrams: Double,
    val designFee: Double,
    val purchasePrice: Double,
    val quantityInStock: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long = 0L
)


data class LocalUser(
    val id: String,
    val name: String,
    val pinHash: String,
    val role: String,
    val isActive: Boolean,
    val createdAt: Long,
    val lastSyncedAt: Long = 0L
)


data class LocalSale(
    val id: Long = 0L,
    val serverId: String? = null,
    val date: LocalDateTime,
    val total: Double,
    val paymentMethod: String,
    val staffId: String,
    val staffName: String,
    val createdAt: Long,
    val isSynced: Boolean = false
)


data class LocalSaleItem(
    val id: Long = 0L,
    val saleId: Long,
    val productSku: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val purchasePrice: Double?,
    val designFee: Double?,
    val profit: Double?
)


data class LocalSaleWithItems(
    val sale: LocalSale,
    val items: List<LocalSaleItem>
)


data class SyncStatus(
    val isOnline: Boolean,
    val lastSyncTimestamp: Long?,
    val pendingSalesCount: Int,
    val syncInProgress: Boolean = false
)


sealed class OfflineQueueItem {
    data class CreateSale(val sale: LocalSale, val items: List<LocalSaleItem>) : OfflineQueueItem()
    data class UpdateProductStock(val sku: String, val newQuantity: Int, val reason: String) : OfflineQueueItem()
    data class CreateUser(val user: LocalUser) : OfflineQueueItem()
    data class UpdateUser(val user: LocalUser) : OfflineQueueItem()
}


interface OfflineRepository {


    suspend fun getProducts(): Flow<List<LocalProduct>>
    suspend fun getProductBySku(sku: String): LocalProduct?
    suspend fun syncProducts(): Result<Unit>


    suspend fun authenticateUser(pin: String): LocalUser?
    suspend fun getUsers(): List<LocalUser>
    suspend fun syncUsers(): Result<Unit>


    suspend fun createSale(sale: LocalSale, items: List<LocalSaleItem>): Result<Long>
    suspend fun getSalesForDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<LocalSaleWithItems>
    suspend fun syncPendingSales(): Result<Unit>


    suspend fun performFullSync(): Result<Unit>
    fun getSyncStatus(): Flow<SyncStatus>
    fun isOnline(): Boolean


    suspend fun addToOfflineQueue(item: OfflineQueueItem)
    suspend fun processOfflineQueue(): Result<Unit>
}