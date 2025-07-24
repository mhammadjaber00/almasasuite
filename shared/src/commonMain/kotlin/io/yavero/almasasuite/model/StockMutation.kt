package io.yavero.almasasuite.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable


@Serializable
data class StockMutation(
    val id: String,
    val productId: String,
    val delta: Int,
    val reason: StockMutationReason,
    val timestamp: Instant
)


@Serializable
enum class StockMutationReason {
    SALE,
    PURCHASE,
    INVENTORY_ADJUSTMENT,
    RETURN,
    DAMAGED,
    LOST,
    TRANSFER,
    OTHER
}