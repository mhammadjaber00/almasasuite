package io.yavero.almasasuite.pos.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.yavero.almasasuite.model.JewelryProduct
import io.yavero.almasasuite.model.JewelryProductRequest
import io.yavero.almasasuite.model.JewelryType
import io.yavero.almasasuite.model.Product
import io.yavero.almasasuite.pos.config.PosConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json


class JewelryProductRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }


    private val config = PosConfig.getInstance()


    private val apiUrl = config.api.baseUrl







    suspend fun getAllJewelryProducts(): List<JewelryProduct> = withContext(Dispatchers.IO) {
        try {

            val response = client.get("$apiUrl/products")


            val serverProducts: List<ServerProductResponse> = response.body()


            return@withContext serverProducts.map { it.toJewelryProduct() }
        } catch (e: Exception) {

            emptyList()
        }
    }


    suspend fun getJewelryProductById(id: String): JewelryProduct? = withContext(Dispatchers.IO) {
        try {
            val response = client.get("$apiUrl/products/$id")
            val serverProduct: ServerProductResponse = response.body()
            return@withContext serverProduct.toJewelryProduct()
        } catch (e: Exception) {

            null
        }
    }


    suspend fun createJewelryProduct(product: JewelryProductRequest): JewelryProduct? = withContext(Dispatchers.IO) {
        try {

            val serverRequest = ServerProductRequest(
                sku = product.sku,
                imageUrl = product.imageUrl,
                type = product.type,
                karat = product.karat,
                weightGrams = product.weightGrams,
                designFee = product.designFee,
                purchasePrice = product.purchasePrice,
                quantityInStock = product.quantityInStock
            )

            val response = client.post("$apiUrl/products") {
                contentType(ContentType.Application.Json)
                setBody(serverRequest)
            }

            if (response.status.isSuccess()) {
                val serverProduct: ServerProductResponse = response.body()
                return@withContext serverProduct.toJewelryProduct()
            } else {
                null
            }
        } catch (e: Exception) {

            null
        }
    }


    suspend fun updateJewelryProduct(id: String, product: JewelryProductRequest): JewelryProduct? = withContext(Dispatchers.IO) {
        try {

            val serverRequest = ServerProductRequest(
                sku = product.sku ?: "",
                imageUrl = product.imageUrl,
                type = product.type,
                karat = product.karat,
                weightGrams = product.weightGrams,
                designFee = product.designFee,
                purchasePrice = product.purchasePrice,
                quantityInStock = product.quantityInStock
            )

            val response = client.put("$apiUrl/products/$id") {
                contentType(ContentType.Application.Json)
                setBody(serverRequest)
            }

            if (response.status.isSuccess()) {
                val serverProduct: ServerProductResponse = response.body()
                return@withContext serverProduct.toJewelryProduct()
            } else {
                null
            }
        } catch (e: Exception) {

            null
        }
    }


    suspend fun adjustJewelryProductStock(id: String, delta: Int): JewelryProduct? = withContext(Dispatchers.IO) {
        try {
            val response = client.patch("$apiUrl/products/$id/stock") {
                contentType(ContentType.Application.Json)
                setBody(StockAdjustmentRequest(delta))
            }

            if (response.status.isSuccess()) {
                val serverProduct: ServerProductResponse = response.body()
                return@withContext serverProduct.toJewelryProduct()
            } else {
                null
            }
        } catch (e: Exception) {

            null
        }
    }


    suspend fun deleteJewelryProduct(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.delete("$apiUrl/products/$id")

            response.status.isSuccess()
        } catch (e: Exception) {

            false
        }
    }




    fun convertToProduct(jewelryProduct: JewelryProduct): Product {
        return jewelryProduct.toProduct()
    }


    fun convertToProducts(jewelryProducts: List<JewelryProduct>): List<Product> {
        return jewelryProducts.map { it.toProduct() }
    }


    fun createRequestFromProduct(product: Product): JewelryProductRequest {
        val jewelryProduct = JewelryProduct.fromProduct(product)
        return JewelryProductRequest(
            sku = jewelryProduct.sku,
            imageUrl = jewelryProduct.imageUrl,
            type = jewelryProduct.type,
            karat = jewelryProduct.karat,
            weightGrams = jewelryProduct.weightGrams,
            designFee = jewelryProduct.designFee,
            purchasePrice = jewelryProduct.purchasePrice,
            quantityInStock = jewelryProduct.quantityInStock
        )
    }


    @kotlinx.serialization.Serializable
    private data class ServerProductResponse(
        val id: String,
        val sku: String,
        val imageUrl: String?,
        val type: JewelryType,
        val karat: Int,
        val weightGrams: Double,
        val quantityInStock: Int
    ) {


        fun toJewelryProduct(): JewelryProduct {


            val estimatedTotalPrice = weightGrams * 50.0
            val designFee = estimatedTotalPrice * 0.3
            val purchasePrice = estimatedTotalPrice * 0.7

            return JewelryProduct(
                id = id,
                sku = sku,
                imageUrl = imageUrl,
                type = type,
                karat = karat,
                weightGrams = weightGrams,
                designFee = designFee,
                purchasePrice = purchasePrice,
                quantityInStock = quantityInStock
            )
        }
    }


    @kotlinx.serialization.Serializable
    private data class ServerProductRequest(
        val sku: String? = null,
        val imageUrl: String? = null,
        val type: JewelryType,
        val karat: Int,
        val weightGrams: Double,
        val designFee: Double,
        val purchasePrice: Double,
        val quantityInStock: Int = 0
    )
}