package io.yavero.almasasuite.pos.service

import io.yavero.almasasuite.pos.config.PosConfig
import io.yavero.almasasuite.pos.ui.sales.SalesLogEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


class SalesSubmissionService {
    private val httpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }
    private val jewelryProductService = JewelryProductService()


    suspend fun submitSales(
        salesLog: List<SalesLogEntry>,
        paymentMethod: String = "CASH"
    ): SalesSubmissionResult = withContext(Dispatchers.IO) {
        try {

            jewelryProductService.loadProducts()


            val saleItems = mutableListOf<SaleItemRequest>()
            val invalidSkus = mutableListOf<String>()

            for (entry in salesLog) {
                val product = jewelryProductService.findProductBySku(entry.sku)
                if (product != null) {

                    if (product.quantityInStock < entry.quantity) {
                        return@withContext SalesSubmissionResult.Error(
                            "Insufficient stock for ${entry.sku}. Available: ${product.quantityInStock}, Requested: ${entry.quantity}"
                        )
                    }

                    saleItems.add(
                        SaleItemRequest(
                            productId = product.id,
                            quantity = entry.quantity,
                            unitPrice = entry.unitPrice
                        )
                    )
                } else {
                    invalidSkus.add(entry.sku)
                }
            }


            if (invalidSkus.isNotEmpty()) {
                return@withContext SalesSubmissionResult.Error(
                    "Products not found for SKUs: ${invalidSkus.joinToString(", ")}"
                )
            }

            val saleRequest = SaleRequest(
                items = saleItems,
                paymentMethod = paymentMethod
            )

            val requestBody = json.encodeToString(SaleRequest.serializer(), saleRequest)

            val request = HttpRequest.newBuilder()
                .uri(URI.create("${PosConfig.getInstance().api.baseUrl}/sales"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                201 -> {

                    val saleResponse = json.decodeFromString(SaleResponse.serializer(), response.body())
                    SalesSubmissionResult.Success(saleResponse)
                }
                400 -> {

                    val errorResponse = json.decodeFromString(ErrorResponse.serializer(), response.body())
                    SalesSubmissionResult.Error("Validation error: ${errorResponse.error}")
                }
                409 -> {

                    val errorResponse = json.decodeFromString(ErrorResponse.serializer(), response.body())
                    SalesSubmissionResult.Error("Insufficient stock: ${errorResponse.error}")
                }
                else -> {
                    SalesSubmissionResult.Error("Server error: ${response.statusCode()}")
                }
            }
        } catch (e: Exception) {
            SalesSubmissionResult.Error("Network error: ${e.message}")
        }
    }
}


sealed class SalesSubmissionResult {
    data class Success(val saleResponse: SaleResponse) : SalesSubmissionResult()
    data class Error(val message: String) : SalesSubmissionResult()
}


@Serializable
data class SaleItemRequest(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double
)


@Serializable
data class SaleRequest(
    val items: List<SaleItemRequest>,
    val paymentMethod: String
)


@Serializable
data class SaleItemResponse(
    val id: String,
    val productId: String,
    val productSku: String,
    val productType: String,
    val productKarat: Int,
    val productWeightGrams: Double,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)


@Serializable
data class SaleResponse(
    val id: String,
    val date: String,
    val total: Double,
    val paymentMethod: String,
    val items: List<SaleItemResponse>
)


@Serializable
data class ErrorResponse(
    val error: String
)