package io.yavero.almasasuite.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class SaleItem(
    val productId: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)


@Serializable
data class Sale(
    val id: String,
    val date: Instant,
    val items: List<SaleItem>,
    val total: Double,
    val paymentMethod: PaymentMethod
)


@Serializable
enum class PaymentMethod {
    CASH,
    CREDIT_CARD,
    DEBIT_CARD,
    BANK_TRANSFER,
    MOBILE_PAYMENT,
    OTHER
}