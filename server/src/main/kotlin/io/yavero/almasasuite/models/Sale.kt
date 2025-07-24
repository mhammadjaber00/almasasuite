package io.yavero.almasasuite.models

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.time.LocalDateTime


object Sales : Table("sales") {
    val id = varchar("id", 36)
    val date = timestamp("date")
    val total = decimal("total", 10, 2)
    val paymentMethod = varchar("payment_method", 50)
    val createdAt = long("created_at")
    val synced = bool("synced")

    override val primaryKey = PrimaryKey(id)
}


object SaleItems : Table("sale_items") {
    val id = varchar("id", 36)
    val saleId = varchar("sale_id", 36) references Sales.id
    val productId = varchar("product_id", 36) references Products.id
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2)
    val subtotal = decimal("subtotal", 10, 2)
    val purchasePrice = decimal("purchase_price", 10, 2).nullable()
    val designFee = decimal("design_fee", 10, 2).nullable()
    val profit = decimal("profit", 10, 2).nullable()

    override val primaryKey = PrimaryKey(id)
}


data class Sale(
    val id: String,
    val date: LocalDateTime,
    val total: Double,
    val paymentMethod: String,
    val createdAt: Long,
    val synced: Boolean,
    val items: List<SaleItem> = emptyList()
)


data class SaleItem(
    val id: String,
    val saleId: String,
    val productId: String,
    val productSku: String,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val purchasePrice: Double? = null,
    val designFee: Double? = null,
    val profit: Double? = null,
    val productName: String? = null,
    val productType: ProductType? = null,
    val productKarat: Int? = null,
    val productWeightGrams: Double? = null
)


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
    val productType: ProductType,
    val productKarat: Int,
    val productWeightGrams: Double,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)


@Serializable
data class SaleItemDetailResponse(
    val id: String,
    val productId: String,
    val productSku: String,
    val productType: ProductType,
    val productKarat: Int,
    val productWeightGrams: Double,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double,
    val purchasePrice: Double,
    val designFee: Double,
    val profit: Double
)


@Serializable
data class SaleResponse(
    val id: String,
    val date: String,
    val total: Double,
    val paymentMethod: String,
    val items: List<SaleItemResponse>
)


fun Sale.toResponse(): SaleResponse {
    return SaleResponse(
        id = id,
        date = date.toString(),
        total = total,
        paymentMethod = paymentMethod,
        items = items.map {
            SaleItemResponse(
                id = it.id,
                productId = it.productId,
                productSku = it.productSku,
                productType = it.productType ?: ProductType.OTHER,
                productKarat = it.productKarat ?: 0,
                productWeightGrams = it.productWeightGrams ?: 0.0,
                quantity = it.quantity,
                unitPrice = it.unitPrice,
                subtotal = it.subtotal
            )
        }
    )
}


@Serializable
data class SaleDetailResponse(
    val id: String,
    val date: String,
    val total: Double,
    val paymentMethod: String,
    val totalProfit: Double,
    val items: List<SaleItemDetailResponse>
)


fun Sale.toDetailResponse(): SaleDetailResponse {
    val itemsWithDetails = items.mapNotNull { item ->
        if (item.purchasePrice != null && item.designFee != null && item.profit != null) {
            SaleItemDetailResponse(
                id = item.id,
                productId = item.productId,
                productSku = item.productSku,
                productType = item.productType ?: ProductType.OTHER,
                productKarat = item.productKarat ?: 0,
                productWeightGrams = item.productWeightGrams ?: 0.0,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                subtotal = item.subtotal,
                purchasePrice = item.purchasePrice,
                designFee = item.designFee,
                profit = item.profit
            )
        } else null
    }

    val totalProfit = itemsWithDetails.sumOf { it.profit }

    return SaleDetailResponse(
        id = id,
        date = date.toString(),
        total = total,
        paymentMethod = paymentMethod,
        totalProfit = totalProfit,
        items = itemsWithDetails
    )
}