package io.yavero.almasasuite.pos.service

import io.yavero.almasasuite.model.JewelryProduct
import io.yavero.almasasuite.model.JewelryProductRequest
import io.yavero.almasasuite.model.JewelryType
import io.yavero.almasasuite.pos.repository.JewelryProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class JewelryProductService {
    private val repository = JewelryProductRepository()

    private val _products = MutableStateFlow<List<JewelryProduct>>(emptyList())
    val products: StateFlow<List<JewelryProduct>> = _products.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()


    suspend fun loadProducts(): Result<List<JewelryProduct>> {
        return try {
            _isLoading.value = true
            _error.value = null

            val products = repository.getAllJewelryProducts()
            _products.value = products
            _isLoading.value = false

            Result.success(products)
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to load products: ${e.message}"
            Result.failure(e)
        }
    }


    suspend fun createProduct(
        sku: String? = null,
        imageUrl: String? = null,
        type: JewelryType,
        karat: Int,
        weightGrams: Double,
        designFee: Double,
        purchasePrice: Double,
        quantityInStock: Int = 0
    ): Result<JewelryProduct> {
        return try {
            _isLoading.value = true
            _error.value = null

            val request = JewelryProductRequest(
                sku = sku,
                imageUrl = imageUrl,
                type = type,
                karat = karat,
                weightGrams = weightGrams,
                designFee = designFee,
                purchasePrice = purchasePrice,
                quantityInStock = quantityInStock
            )

            val createdProduct = repository.createJewelryProduct(request)
            if (createdProduct != null) {

                loadProducts()
                Result.success(createdProduct)
            } else {
                _isLoading.value = false
                _error.value = "Failed to create product"
                Result.failure(Exception("Failed to create product"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to create product: ${e.message}"
            Result.failure(e)
        }
    }


    suspend fun updateProduct(
        id: String,
        sku: String,
        imageUrl: String? = null,
        type: JewelryType,
        karat: Int,
        weightGrams: Double,
        designFee: Double,
        purchasePrice: Double,
        quantityInStock: Int
    ): Result<JewelryProduct> {
        return try {
            _isLoading.value = true
            _error.value = null

            val request = JewelryProductRequest(
                sku = sku,
                imageUrl = imageUrl,
                type = type,
                karat = karat,
                weightGrams = weightGrams,
                designFee = designFee,
                purchasePrice = purchasePrice,
                quantityInStock = quantityInStock
            )

            val updatedProduct = repository.updateJewelryProduct(id, request)
            if (updatedProduct != null) {

                loadProducts()
                Result.success(updatedProduct)
            } else {
                _isLoading.value = false
                _error.value = "Failed to update product"
                Result.failure(Exception("Failed to update product"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to update product: ${e.message}"
            Result.failure(e)
        }
    }


    suspend fun adjustStock(id: String, delta: Int): Result<JewelryProduct> {
        return try {
            _isLoading.value = true
            _error.value = null

            val updatedProduct = repository.adjustJewelryProductStock(id, delta)
            if (updatedProduct != null) {

                loadProducts()
                Result.success(updatedProduct)
            } else {
                _isLoading.value = false
                _error.value = "Failed to adjust stock"
                Result.failure(Exception("Failed to adjust stock"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to adjust stock: ${e.message}"
            Result.failure(e)
        }
    }


    suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            _isLoading.value = true
            _error.value = null

            val success = repository.deleteJewelryProduct(id)
            if (success) {

                loadProducts()
                Result.success(Unit)
            } else {
                _isLoading.value = false
                _error.value = "Failed to delete product"
                Result.failure(Exception("Failed to delete product"))
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _error.value = "Failed to delete product: ${e.message}"
            Result.failure(e)
        }
    }


    fun findProductById(id: String): JewelryProduct? {
        return _products.value.find { it.id == id }
    }


    fun findProductBySku(sku: String): JewelryProduct? {
        return _products.value.find { it.sku == sku }
    }


    fun clearError() {
        _error.value = null
    }
}