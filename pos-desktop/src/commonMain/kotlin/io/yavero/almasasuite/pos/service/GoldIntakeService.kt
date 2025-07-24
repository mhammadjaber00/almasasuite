package io.yavero.almasasuite.pos.service

import io.yavero.almasasuite.pos.config.PosConfig
import io.yavero.almasasuite.model.GoldIntakeRequest
import io.yavero.almasasuite.model.PartyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


@Serializable
data class ApiErrorResponse(
    val error: String
)


class GoldIntakeService {
    private val httpClient = HttpClient.newBuilder().build()
    private val json = Json { ignoreUnknownKeys = true }
    private val config = PosConfig.getInstance()


    suspend fun submitGoldIntake(
        request: GoldIntakeRequest
    ): GoldIntakeResult = withContext(Dispatchers.IO) {
        try {

            val requestBody = json.encodeToString(GoldIntakeRequest.serializer(), request)

            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("${config.api.baseUrl}/gold-intake/intakes"))
                .header("Content-Type", "application/json")
                .header(config.security.managerPinHeader, config.security.managerPin)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                201 -> {
                    val intakeResponse = json.decodeFromString(GoldIntakeResponse.serializer(), response.body())
                    GoldIntakeResult.Success(intakeResponse)
                }
                400 -> {
                    val errorResponse = json.decodeFromString(ApiErrorResponse.serializer(), response.body())
                    GoldIntakeResult.Error("Validation error: ${errorResponse.error}")
                }
                401 -> {
                    GoldIntakeResult.Error("Unauthorized: Manager access required")
                }
                else -> {
                    GoldIntakeResult.Error("Server error: ${response.statusCode()}")
                }
            }
        } catch (e: Exception) {
            GoldIntakeResult.Error("Network error: ${e.message}")
        }
    }


    suspend fun submitVendorPayment(
        vendorId: String,
        amount: Double,
        notes: String? = null
    ): VendorPaymentResult = withContext(Dispatchers.IO) {
        try {
            val request = VendorPaymentRequest(
                vendorId = vendorId,
                amount = amount,
                notes = notes
            )

            val requestBody = json.encodeToString(VendorPaymentRequest.serializer(), request)

            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("${config.api.baseUrl}/gold-intake/payments"))
                .header("Content-Type", "application/json")
                .header(config.security.managerPinHeader, config.security.managerPin)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                201 -> {
                    val paymentResponse = json.decodeFromString(VendorPaymentResponse.serializer(), response.body())
                    VendorPaymentResult.Success(paymentResponse)
                }
                400 -> {
                    val errorResponse = json.decodeFromString(ApiErrorResponse.serializer(), response.body())
                    VendorPaymentResult.Error("Validation error: ${errorResponse.error}")
                }
                401 -> {
                    VendorPaymentResult.Error("Unauthorized: Manager access required")
                }
                else -> {
                    VendorPaymentResult.Error("Server error: ${response.statusCode()}")
                }
            }
        } catch (e: Exception) {
            VendorPaymentResult.Error("Network error: ${e.message}")
        }
    }


    suspend fun loadVendors(): VendorsResult = withContext(Dispatchers.IO) {
        try {
            val httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("${config.api.baseUrl}/gold-intake/vendors"))
                .header(config.security.managerPinHeader, config.security.managerPin)
                .GET()
                .build()

            val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

            when (response.statusCode()) {
                200 -> {
                    val vendors = json.decodeFromString<List<VendorResponse>>(response.body())
                    VendorsResult.Success(vendors)
                }
                401 -> {
                    VendorsResult.Error("Unauthorized: Manager access required")
                }
                else -> {
                    VendorsResult.Error("Server error: ${response.statusCode()}")
                }
            }
        } catch (e: Exception) {
            VendorsResult.Error("Network error: ${e.message}")
        }
    }
}


sealed class GoldIntakeResult {
    data class Success(val intake: GoldIntakeResponse) : GoldIntakeResult()
    data class Error(val message: String) : GoldIntakeResult()
}

sealed class VendorPaymentResult {
    data class Success(val payment: VendorPaymentResponse) : VendorPaymentResult()
    data class Error(val message: String) : VendorPaymentResult()
}

sealed class VendorsResult {
    data class Success(val vendors: List<VendorResponse>) : VendorsResult()
    data class Error(val message: String) : VendorsResult()
}


@Serializable
data class GoldIntakeResponse(
    val id: String,
    val vendorId: String,
    val karat: Int,
    val grams: Double,
    val designFeePerGram: Double,
    val metalValuePerGram: Double,
    val totalDesignFee: Double,
    val totalMetalValue: Double,
    val recordedAt: String,
    val recordedBy: String
)

@Serializable
data class VendorPaymentRequest(
    val vendorId: String,
    val amount: Double,
    val notes: String? = null
)

@Serializable
data class VendorPaymentResponse(
    val id: String,
    val vendorId: String,
    val amount: Double,
    val notes: String?,
    val recordedAt: String,
    val recordedBy: String
)

@Serializable
data class VendorResponse(
    val id: String,
    val name: String,
    val contactInfo: String?,
    val totalLiabilityBalance: Double,
    val createdAt: String
)
