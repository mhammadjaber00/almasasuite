package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
enum class JewelryType {
    RING, BRACELET, NECKLACE, EARRING, OTHER;

    companion object {
        fun fromString(value: String): JewelryType {
            return when (value.lowercase()) {
                "ring" -> RING
                "bracelet" -> BRACELET
                "necklace" -> NECKLACE
                "earring" -> EARRING
                else -> OTHER
            }
        }

        fun toDbValue(type: JewelryType): String {
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


@Serializable
data class JewelryProduct(
    val id: String,
    val sku: String,
    val imageUrl: String? = null,
    val type: JewelryType,
    val karat: Int,
    val weightGrams: Double,
    val designFee: Double,
    val purchasePrice: Double,
    val quantityInStock: Int,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {


    val totalPrice: Double
        get() = designFee + purchasePrice


    val displayName: String
        get() = "${type.name} ${karat}K ${weightGrams}g"


    fun toProduct(): Product {
        return Product(
            id = id,
            sku = sku,
            name = displayName,
            stone = "Gold ${karat}K",
            carat = karat.toDouble(),
            weight = weightGrams,
            price = totalPrice,
            quantityInStock = quantityInStock
        )
    }

    companion object {


        fun fromProduct(product: Product): JewelryProduct {

            val type = when {
                product.name.contains("ring", ignoreCase = true) -> JewelryType.RING
                product.name.contains("bracelet", ignoreCase = true) -> JewelryType.BRACELET
                product.name.contains("necklace", ignoreCase = true) -> JewelryType.NECKLACE
                product.name.contains("earring", ignoreCase = true) -> JewelryType.EARRING
                else -> JewelryType.OTHER
            }


            val designFee = product.price * 0.3
            val purchasePrice = product.price * 0.7

            return JewelryProduct(
                id = product.id,
                sku = product.sku,
                imageUrl = null,
                type = type,
                karat = product.carat.toInt(),
                weightGrams = product.weight,
                designFee = designFee,
                purchasePrice = purchasePrice,
                quantityInStock = product.quantityInStock
            )
        }
    }
}


@Serializable
data class JewelryProductRequest(
    val sku: String? = null,
    val imageUrl: String? = null,
    val type: JewelryType,
    val karat: Int,
    val weightGrams: Double,
    val designFee: Double,
    val purchasePrice: Double,
    val quantityInStock: Int = 0
)