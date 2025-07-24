package io.yavero.almasasuite.pos.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.yavero.almasasuite.model.Product
import io.yavero.almasasuite.model.ProductType
import io.yavero.almasasuite.pos.config.PosConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class ProductRequest(
    val sku: String? = null,
    val imageUrl: String? = null,
    val type: ProductType,
    val karat: Int,
    val weightGrams: Double,
    val designFee: Double,
    val purchasePrice: Double,
    val quantityInStock: Int = 0
)


@Serializable
data class StockAdjustmentRequest(
    val delta: Int
)


fun createProductFromLegacyFormat(
    id: String,
    sku: String,
    name: String,
    stone: String,
    carat: Double,
    weight: Double,
    price: Double,
    quantityInStock: Int
): Product {

    val type = when {
        name.contains("ring", ignoreCase = true) -> ProductType.RING
        name.contains("bracelet", ignoreCase = true) -> ProductType.BRACELET
        name.contains("necklace", ignoreCase = true) -> ProductType.NECKLACE
        name.contains("earring", ignoreCase = true) -> ProductType.EARRING
        else -> ProductType.OTHER
    }


    val designFee = price * 0.3
    val purchasePrice = price * 0.7

    return Product(
        id = id,
        sku = sku,
        name = name,
        stone = stone,
        carat = carat,
        weight = weight,
        price = price,
        quantityInStock = quantityInStock,
        imageUrl = null,
        type = type
    )
}


fun Product.toProductRequest(): ProductRequest {
    return ProductRequest(
        sku = this.sku,
        imageUrl = this.imageUrl,
        type = this.type,
        karat = this.karat,
        weightGrams = this.weightGrams,
        designFee = this.designFee,
        purchasePrice = this.purchasePrice,
        quantityInStock = this.quantityInStock
    )
}


class ProductRepository {

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






    suspend fun getAllProducts(): List<Product> = withContext(Dispatchers.IO) {
        try {
            client.get("$apiUrl/products").body()
        } catch (e: Exception) {

            emptyList()
        }
    }


    suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        try {
            client.get("$apiUrl/products/$id").body()
        } catch (e: Exception) {

            null
        }
    }


    suspend fun searchProductsByName(query: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            client.get("$apiUrl/products/search") {
                parameter("q", query)
            }.body()
        } catch (e: Exception) {

            emptyList()
        }
    }


    @Deprecated("Use adjustProductStock instead", ReplaceWith("adjustProductStock(id, newQuantity - currentQuantity)"))
    suspend fun updateProductStock(id: String, newQuantity: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.put("$apiUrl/products/$id") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("quantityInStock" to newQuantity))
            }
            response.status.isSuccess()
        } catch (e: Exception) {

            false
        }
    }


    suspend fun createProduct(product: ProductRequest): Product? = withContext(Dispatchers.IO) {
        try {
            val response = client.post("$apiUrl/products") {
                contentType(ContentType.Application.Json)
                setBody(product)
            }

            if (response.status.isSuccess()) {
                response.body<Product>()
            } else {
                null
            }
        } catch (e: Exception) {

            null
        }
    }


    suspend fun updateProduct(id: String, product: ProductRequest): Product? = withContext(Dispatchers.IO) {
        try {
            val response = client.put("$apiUrl/products/$id") {
                contentType(ContentType.Application.Json)
                setBody(product)
            }

            if (response.status.isSuccess()) {
                response.body<Product>()
            } else {
                null
            }
        } catch (e: Exception) {

            null
        }
    }


    suspend fun adjustProductStock(id: String, delta: Int): Product? = withContext(Dispatchers.IO) {
        try {
            val response = client.patch("$apiUrl/products/$id/stock") {
                contentType(ContentType.Application.Json)
                setBody(StockAdjustmentRequest(delta))
            }

            if (response.status.isSuccess()) {
                response.body<Product>()
            } else {
                null
            }
        } catch (e: Exception) {

            null
        }
    }


}