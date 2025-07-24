package io.yavero.almasasuite.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table


@Serializable
enum class ProductType {
    RING, BRACELET, NECKLACE, EARRING, OTHER;

    companion object {
        fun fromString(value: String): ProductType {
            return when (value.lowercase()) {
                "ring" -> RING
                "bracelet" -> BRACELET
                "necklace" -> NECKLACE
                "earring" -> EARRING
                else -> OTHER
            }
        }

        fun toDbValue(type: ProductType): String {
            return when (type) {
                RING -> "ring"
                BRACELET -> "bracelet"
                NECKLACE -> "necklace"
                EARRING -> "earring"
                OTHER -> "other"
            }
        }
    }
}


object Products : Table("products_new") {
    val id = varchar("id", 36)
    val sku = varchar("sku", 50)
    val imageUrl = varchar("image_url", 255).nullable()
    val type = varchar("type", 20)
    val karat = integer("karat")
    val weightGrams = decimal("weight_grams", 10, 2)
    val designFee = decimal("design_fee", 10, 2)
    val purchasePrice = decimal("purchase_price", 10, 2)
    val quantityInStock = integer("quantity_in_stock")
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(id)
}


data class Product(
    val id: String,
    val sku: String,
    val imageUrl: String?,
    val type: ProductType,
    val karat: Int,
    val weightGrams: Double,
    val designFee: Double,
    val purchasePrice: Double,
    val quantityInStock: Int,
    val createdAt: Long,
    val updatedAt: Long
)


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
data class ProductResponse(
    val id: String,
    val sku: String,
    val imageUrl: String?,
    val type: ProductType,
    val karat: Int,
    val weightGrams: Double,
    val quantityInStock: Int
)


@Serializable
data class ProductDetailResponse(
    val id: String,
    val sku: String,
    val imageUrl: String?,
    val type: ProductType,
    val karat: Int,
    val weightGrams: Double,
    val designFee: Double,
    val purchasePrice: Double,
    val quantityInStock: Int
)


@Serializable
data class StockAdjustmentRequest(
    val delta: Int
)


@Serializable
data class SkuInfo(
    val type: ProductType,
    val karat: Int,
    val weightGrams: Double,
    val sequence: Int
)


fun Product.toResponse(): ProductResponse {
    return ProductResponse(
        id = id,
        sku = sku,
        imageUrl = imageUrl,
        type = type,
        karat = karat,
        weightGrams = weightGrams,
        quantityInStock = quantityInStock
    )
}


fun Product.toDetailResponse(): ProductDetailResponse {
    return ProductDetailResponse(
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