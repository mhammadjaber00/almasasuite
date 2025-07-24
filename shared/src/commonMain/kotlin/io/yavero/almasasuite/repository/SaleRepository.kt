package io.yavero.almasasuite.repository

import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.db.Sale as DbSale
import io.yavero.almasasuite.db.SaleItem as DbSaleItem
import io.yavero.almasasuite.model.PaymentMethod
import io.yavero.almasasuite.model.Sale
import io.yavero.almasasuite.model.SaleItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers


interface SaleRepository {


    fun getAllSales(): Flow<List<Sale>>


    suspend fun getSaleById(id: String): Sale?


    suspend fun getSaleWithItems(id: String): Sale?


    suspend fun getSalesByDateRange(start: Instant, end: Instant): List<Sale>


    suspend fun getUnsyncedSales(): List<Sale>


    suspend fun createSale(sale: Sale): Boolean


    suspend fun markSaleAsSynced(id: String): Boolean


    suspend fun deleteSale(id: String): Boolean
}


class SaleRepositoryImpl(
    private val database: AlmasaDatabase,
    private val stockMutationRepository: StockMutationRepository
) : SaleRepository {

    override fun getAllSales(): Flow<List<Sale>> {
        return flow {
            val sales = database.saleQueries
                .getAllSales()
                .executeAsList()
                .map { saleEntity ->
                    val items = database.saleQueries
                        .getSaleItemsBySaleId(saleEntity.id)
                        .executeAsList()
                        .map { it.toSaleItem() }
                    saleEntity.toSale(items)
                }
            emit(sales)
        }
    }

    override suspend fun getSaleById(id: String): Sale? {
        val saleEntity = database.saleQueries
            .getSaleById(id)
            .executeAsOneOrNull() ?: return null

        val items = database.saleQueries
            .getSaleItemsBySaleId(id)
            .executeAsList()
            .map { it.toSaleItem() }

        return saleEntity.toSale(items)
    }

    override suspend fun getSaleWithItems(id: String): Sale? {
        val saleEntity = database.saleQueries
            .getSaleById(id)
            .executeAsOneOrNull() ?: return null

        val items = database.saleQueries
            .getSaleItemsBySaleId(id)
            .executeAsList()
            .map { it.toSaleItem() }

        return saleEntity.toSale(items)
    }

    override suspend fun getSalesByDateRange(start: Instant, end: Instant): List<Sale> {
        val startMillis = start.toEpochMilliseconds()
        val endMillis = end.toEpochMilliseconds()

        return database.saleQueries
            .getSalesByDateRange(startMillis, endMillis)
            .executeAsList()
            .map { saleEntity ->
                val items = database.saleQueries
                    .getSaleItemsBySaleId(saleEntity.id)
                    .executeAsList()
                    .map { it.toSaleItem() }
                saleEntity.toSale(items)
            }
    }

    override suspend fun getUnsyncedSales(): List<Sale> {
        return database.saleQueries
            .getUnsyncedSales()
            .executeAsList()
            .map { saleEntity ->
                val items = database.saleQueries
                    .getSaleItemsBySaleId(saleEntity.id)
                    .executeAsList()
                    .map { it.toSaleItem() }
                saleEntity.toSale(items)
            }
    }

    override suspend fun createSale(sale: Sale): Boolean {
        return try {

            val stockMutations = sale.items.map { item ->
                Triple(item.productId, -item.quantity, sale.date)
            }


            database.transaction {
                val now = Clock.System.now().toEpochMilliseconds()


                database.saleQueries.insertSale(
                    id = sale.id,
                    date = sale.date.toEpochMilliseconds(),
                    total = sale.total,
                    payment_method = sale.paymentMethod.name,
                    created_at = now,
                    synced = 0L
                )


                sale.items.forEach { item ->
                    val itemId = "${sale.id}_${item.productId}"
                    database.saleQueries.insertSaleItem(
                        id = itemId,
                        sale_id = sale.id,
                        product_id = item.productId,
                        quantity = item.quantity.toLong(),
                        unit_price = item.unitPrice,
                        subtotal = item.subtotal
                    )


                    val product = database.productQueries
                        .getProductById(item.productId)
                        .executeAsOne()


                    val newQuantity = (product.quantity_in_stock - item.quantity.toLong()).toInt()
                    database.productQueries.updateProductStock(
                        quantity_in_stock = newQuantity.toLong(),
                        updated_at = now,
                        id = item.productId
                    )
                }
            }


            stockMutations.forEach { (productId, delta, timestamp) ->
                stockMutationRepository.createStockMutation(
                    productId = productId,
                    delta = delta,
                    reason = "SALE",
                    timestamp = timestamp
                )
            }

            true
        } catch (e: Exception) {

            false
        }
    }

    override suspend fun markSaleAsSynced(id: String): Boolean {
        return try {
            database.saleQueries.markSaleAsSynced(id)
            true
        } catch (e: Exception) {

            false
        }
    }

    override suspend fun deleteSale(id: String): Boolean {
        return try {
            database.saleQueries.deleteSale(id)
            true
        } catch (e: Exception) {

            false
        }
    }


    private fun DbSale.toSale(items: List<SaleItem>): Sale {
        return Sale(
            id = id,
            date = Instant.fromEpochMilliseconds(date),
            items = items,
            total = total,
            paymentMethod = PaymentMethod.valueOf(payment_method)
        )
    }


    private fun DbSaleItem.toSaleItem(): SaleItem {
        return SaleItem(
            productId = product_id,
            quantity = quantity.toInt(),
            unitPrice = unit_price,
            subtotal = subtotal
        )
    }
}