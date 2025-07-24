package io.yavero.almasasuite.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.time.LocalDateTime


object StockMutations : Table("stock_mutations") {
    val id = varchar("id", 36)
    val productId = varchar("product_id", 36) references Products.id
    val delta = integer("delta")
    val reason = varchar("reason", 50)
    val timestamp = timestamp("timestamp")
    val createdAt = long("created_at")
    val synced = bool("synced")

    override val primaryKey = PrimaryKey(id)
}


data class StockMutation(
    val id: String,
    val productId: String,
    val delta: Int,
    val reason: String,
    val timestamp: LocalDateTime,
    val createdAt: Long,
    val synced: Boolean,
    val productName: String? = null
)


@Serializable
data class StockMutationRequest(
    val productId: String,
    val delta: Int,
    val reason: String
)


@Serializable
data class StockMutationResponse(
    val id: String,
    val productId: String,
    val productName: String?,
    val delta: Int,
    val reason: String,
    val timestamp: String
)


fun StockMutation.toResponse(): StockMutationResponse {
    return StockMutationResponse(
        id = id,
        productId = productId,
        productName = productName,
        delta = delta,
        reason = reason,
        timestamp = timestamp.toString()
    )
}