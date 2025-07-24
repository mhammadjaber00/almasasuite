package io.yavero.almasasuite.services

import io.yavero.almasasuite.models.*
import io.yavero.almasasuite.plugins.dbQuery
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


class SalesService {


    private fun kotlinx.datetime.Instant.toLocalDateTime(): LocalDateTime =
        LocalDateTime.ofInstant(this.toJavaInstant(), ZoneId.systemDefault())


    private fun LocalDateTime.toKotlinInstant(): Instant =
        java.time.Instant.from(this.atZone(ZoneId.systemDefault())).toKotlinInstant()


    private fun mapRowToSale(row: ResultRow): Sale {
        return Sale(
            id = row[Sales.id],
            date = row[Sales.date].toLocalDateTime(),
            total = row[Sales.total].toDouble(),
            paymentMethod = row[Sales.paymentMethod],
            createdAt = row[Sales.createdAt],
            synced = row[Sales.synced],
            items = emptyList()
        )
    }


    private fun mapRowToSaleItem(row: ResultRow): SaleItem {
        return SaleItem(
            id = row[SaleItems.id],
            saleId = row[SaleItems.saleId],
            productId = row[SaleItems.productId],
            productSku = row[Products.sku],
            quantity = row[SaleItems.quantity],
            unitPrice = row[SaleItems.unitPrice].toDouble(),
            subtotal = row[SaleItems.subtotal].toDouble(),
            purchasePrice = row[SaleItems.purchasePrice]?.toDouble(),
            designFee = row[SaleItems.designFee]?.toDouble(),
            profit = row[SaleItems.profit]?.toDouble(),
            productType = ProductType.fromString(row[Products.type]),
            productKarat = row[Products.karat],
            productWeightGrams = row[Products.weightGrams].toDouble()
        )
    }


    private suspend fun getSaleItems(saleId: String): List<SaleItem> = dbQuery {
        SaleItems
            .join(Products, JoinType.LEFT, SaleItems.productId, Products.id)
            .selectAll().where { SaleItems.saleId eq saleId }
            .map { mapRowToSaleItem(it) }
    }


    fun validateSaleRequest(request: SaleRequest): String? {
        return when {
            request.items.isEmpty() -> "Sale must have at least one item"
            request.items.any { it.quantity <= 0 } -> "All items must have positive quantity"
            request.items.any { it.unitPrice < 0 } -> "Unit price cannot be negative"
            request.paymentMethod.isBlank() -> "Payment method is required"
            else -> null
        }
    }


    suspend fun getAllSales(): List<SaleResponse> = dbQuery {
        val salesData = Sales.selectAll()
            .orderBy(Sales.date, SortOrder.DESC)
            .map { mapRowToSale(it) }

        salesData.map { sale ->
            val items = getSaleItems(sale.id)
            sale.copy(items = items).toResponse()
        }
    }


    suspend fun getSaleById(id: String): SaleResponse? = dbQuery {
        val saleData = Sales.selectAll().where { Sales.id eq id }
            .map { mapRowToSale(it) }
            .singleOrNull() ?: return@dbQuery null

        val items = getSaleItems(id)
        saleData.copy(items = items).toResponse()
    }


    suspend fun saleExists(id: String): Boolean = dbQuery {
        Sales.selectAll().where { Sales.id eq id }.count() > 0
    }


    suspend fun checkProductAvailability(productId: String, requiredQuantity: Int): Pair<Boolean, Int> = dbQuery {
        val product = Products.selectAll().where { Products.id eq productId }
            .map { it[Products.quantityInStock] }
            .singleOrNull()

        if (product != null) {
            Pair(product >= requiredQuantity, product)
        } else {
            Pair(false, 0)
        }
    }


    private suspend fun calculateProfit(productId: String, quantity: Int, unitPrice: Double): Double = dbQuery {
        val product = Products.selectAll().where { Products.id eq productId }
            .map {
                Pair(
                    it[Products.purchasePrice].toDouble(),
                    it[Products.designFee].toDouble()
                )
            }
            .singleOrNull()

        if (product != null) {
            val (purchasePrice, designFee) = product
            val totalCost = (purchasePrice + designFee) * quantity
            val totalRevenue = unitPrice * quantity
            totalRevenue - totalCost
        } else {
            0.0
        }
    }


    suspend fun createSale(request: SaleRequest): SaleResponse = dbQuery {
        val saleId = UUID.randomUUID().toString()
        val now = Clock.System.now().toEpochMilliseconds()
        val saleDate = Clock.System.now()


        val calculatedTotal = request.items.sumOf { it.quantity * it.unitPrice }


        Sales.insert {
            it[id] = saleId
            it[date] = saleDate
            it[total] = calculatedTotal.toBigDecimal()
            it[paymentMethod] = request.paymentMethod
            it[createdAt] = now
            it[synced] = true
        }


        val items = request.items.map { item ->
            val itemId = UUID.randomUUID().toString()
            val itemSubtotal = item.quantity * item.unitPrice
            val profit = calculateProfit(item.productId, item.quantity, item.unitPrice)


            val product = Products.selectAll().where { Products.id eq item.productId }
                .map {
                    Triple(
                        it[Products.sku],
                        it[Products.purchasePrice].toDouble(),
                        it[Products.designFee].toDouble()
                    )
                }
                .single()

            val (sku, purchasePrice, designFee) = product

            SaleItems.insert {
                it[id] = itemId
                it[SaleItems.saleId] = saleId
                it[SaleItems.productId] = item.productId
                it[SaleItems.quantity] = item.quantity
                it[SaleItems.unitPrice] = item.unitPrice.toBigDecimal()
                it[SaleItems.subtotal] = itemSubtotal.toBigDecimal()
                it[SaleItems.purchasePrice] = purchasePrice.toBigDecimal()
                it[SaleItems.designFee] = designFee.toBigDecimal()
                it[SaleItems.profit] = profit.toBigDecimal()
            }


            val currentStock = Products.selectAll().where { Products.id eq item.productId }
                .map { it[Products.quantityInStock] }
                .single()

            Products.update({ Products.id eq item.productId }) {
                it[quantityInStock] = currentStock - item.quantity
                it[updatedAt] = now
            }


            StockMutations.insert {
                it[id] = UUID.randomUUID().toString()
                it[productId] = item.productId
                it[delta] = -item.quantity
                it[reason] = "SALE"
                it[timestamp] = saleDate
                it[createdAt] = now
                it[synced] = true
            }

            SaleItem(
                id = itemId,
                saleId = saleId,
                productId = item.productId,
                productSku = sku,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                subtotal = itemSubtotal,
                purchasePrice = purchasePrice,
                designFee = designFee,
                profit = profit,
                productType = ProductType.OTHER,
                productKarat = 0,
                productWeightGrams = 0.0
            )
        }

        Sale(
            id = saleId,
            date = saleDate.toLocalDateTime(),
            total = calculatedTotal,
            paymentMethod = request.paymentMethod,
            createdAt = now,
            synced = true,
            items = items
        ).toResponse()
    }


    suspend fun deleteSale(id: String): Boolean = dbQuery {

        val items = SaleItems.selectAll().where { SaleItems.saleId eq id }
            .map {
                Pair(it[SaleItems.productId], it[SaleItems.quantity])
            }

        val now = Clock.System.now().toEpochMilliseconds()

        items.forEach { (productId, quantity) ->

            val currentStock = Products.selectAll().where { Products.id eq productId }
                .map { it[Products.quantityInStock] }
                .single()

            Products.update({ Products.id eq productId }) {
                it[quantityInStock] = currentStock + quantity
                it[updatedAt] = now
            }


            StockMutations.insert {
                it[StockMutations.id] = UUID.randomUUID().toString()
                it[StockMutations.productId] = productId
                it[delta] = quantity
                it[reason] = "SALE_REFUND"
                it[timestamp] = Clock.System.now()
                it[createdAt] = now
                it[synced] = true
            }
        }


        SaleItems.deleteWhere { SaleItems.saleId eq id }


        Sales.deleteWhere { Sales.id eq id } > 0
    }


    suspend fun getSalesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<SaleResponse> = dbQuery {
        val startInstant = startDate.toKotlinInstant()
        val endInstant = endDate.toKotlinInstant()

        val salesData = Sales.selectAll()
            .where { (Sales.date greaterEq startInstant) and (Sales.date lessEq endInstant) }
        .orderBy(Sales.date, SortOrder.DESC)
        .map { mapRowToSale(it) }

        salesData.map { sale ->
            val items = getSaleItems(sale.id)
            sale.copy(items = items).toResponse()
        }
    }
}