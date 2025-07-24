package io.yavero.almasasuite.pos.viewmodel

import io.yavero.almasasuite.model.Product


enum class PosTab {
    SALES,
}


data class CartItem(
    val product: Product,
    val quantity: Int,
    val unitPrice: Double,
    val subtotal: Double
)