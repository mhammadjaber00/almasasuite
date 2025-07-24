package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
data class Product(
    val id: String,
    val sku: String,
    val name: String,
    val stone: String,
    val carat: Double,
    val weight: Double,
    val price: Double,
    val quantityInStock: Int,
    val imageUrl: String? = null,
    val type: ProductType = ProductType.OTHER,
    val karat: Int = carat.toInt(),
    val weightGrams: Double = weight,
    val designFee: Double = price * 0.3,
    val purchasePrice: Double = price * 0.7
)