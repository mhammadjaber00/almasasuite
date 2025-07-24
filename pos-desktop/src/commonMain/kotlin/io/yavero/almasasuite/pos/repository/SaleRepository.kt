package io.yavero.almasasuite.pos.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.pos.config.PosConfig
import io.yavero.almasasuite.model.PaymentMethod
import io.yavero.almasasuite.model.Sale
import io.yavero.almasasuite.model.SaleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID


class SaleRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }


    private val config = PosConfig.getInstance()


    private val apiUrl = config.api.baseUrl


    private val database: AlmasaDatabase by lazy {
        val databasePath = config.database.path
        val driver = createSqlDriver(databasePath)
        AlmasaDatabase(driver)
    }


    suspend fun createSale(sale: Sale): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = client.post("$apiUrl/sales") {
                contentType(ContentType.Application.Json)
                setBody(sale)
            }
            response.status.isSuccess()
        } catch (e: Exception) {

            queueSaleForLater(sale)
            false
        }
    }


    suspend fun queueSaleForLater(sale: Sale): Boolean = withContext(Dispatchers.IO) {
        try {

            val existingSale = database.saleQueries
                .getSaleById(sale.id)
                .executeAsOneOrNull()

            if (existingSale != null) {
                println("Sale ${sale.id} already exists in the database, skipping queue")
                return@withContext true
            }


            val now = Clock.System.now().toEpochMilliseconds()

            database.transaction {

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
                        .executeAsOneOrNull()

                    if (product != null) {
                        val newQuantity = (product.quantity_in_stock - item.quantity.toLong()).toInt()
                        database.productQueries.updateProductStock(
                            quantity_in_stock = newQuantity.toLong(),
                            updated_at = now,
                            id = item.productId
                        )
                    }
                }
            }


            sale.items.forEach { item ->
                val mutationId = UUID.randomUUID().toString()
                database.stockMutationQueries.insertStockMutation(
                    id = mutationId,
                    product_id = item.productId,
                    delta = (-item.quantity).toLong(),
                    reason = "SALE",
                    timestamp = sale.date.toEpochMilliseconds(),
                    created_at = now,
                    synced = 0L
                )
            }

            println("Sale ${sale.id} queued for later sync")
            true
        } catch (e: Exception) {
            println("Error queueing sale: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    fun getQueuedSales(): Flow<List<Sale>> = flow {
        try {
            val unsyncedSales = database.saleQueries
                .getUnsyncedSales()
                .executeAsList()
                .map { saleEntity ->
                    val items = database.saleQueries
                        .getSaleItemsBySaleId(saleEntity.id)
                        .executeAsList()
                        .map { itemEntity ->
                            SaleItem(
                                productId = itemEntity.product_id,
                                quantity = itemEntity.quantity.toInt(),
                                unitPrice = itemEntity.unit_price,
                                subtotal = itemEntity.subtotal
                            )
                        }

                    Sale(
                        id = saleEntity.id,
                        date = Instant.fromEpochMilliseconds(saleEntity.date),
                        items = items,
                        total = saleEntity.total,
                        paymentMethod = PaymentMethod.valueOf(saleEntity.payment_method)
                    )
                }

            emit(unsyncedSales)
        } catch (e: Exception) {
            println("Error getting queued sales: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }


    suspend fun syncQueuedSales(): Boolean = withContext(Dispatchers.IO) {
        try {
            var allSuccess = true


            val unsyncedSales = database.saleQueries
                .getUnsyncedSales()
                .executeAsList()
                .map { saleEntity ->
                    val items = database.saleQueries
                        .getSaleItemsBySaleId(saleEntity.id)
                        .executeAsList()
                        .map { itemEntity ->
                            SaleItem(
                                productId = itemEntity.product_id,
                                quantity = itemEntity.quantity.toInt(),
                                unitPrice = itemEntity.unit_price,
                                subtotal = itemEntity.subtotal
                            )
                        }

                    Sale(
                        id = saleEntity.id,
                        date = Instant.fromEpochMilliseconds(saleEntity.date),
                        items = items,
                        total = saleEntity.total,
                        paymentMethod = PaymentMethod.valueOf(saleEntity.payment_method)
                    )
                }

            println("Found ${unsyncedSales.size} unsynced sales to sync")


            for (sale in unsyncedSales) {
                try {
                    val response = client.post("$apiUrl/sales") {
                        contentType(ContentType.Application.Json)
                        setBody(sale)
                    }

                    if (response.status.isSuccess()) {

                        database.saleQueries.markSaleAsSynced(sale.id)


                        sale.items.forEach { item ->
                            database.stockMutationQueries
                                .getStockMutationsByProductId(item.productId)
                                .executeAsList()
                                .filter { it.reason == "SALE" }
                                .forEach { mutation ->
                                    database.stockMutationQueries.markStockMutationAsSynced(mutation.id)
                                }
                        }

                        println("Sale ${sale.id} synced successfully")
                    } else {
                        println("Failed to sync sale ${sale.id}: ${response.status}")
                        allSuccess = false
                    }
                } catch (e: Exception) {
                    println("Error syncing sale ${sale.id}: ${e.message}")
                    e.printStackTrace()
                    allSuccess = false
                }
            }

            allSuccess
        } catch (e: Exception) {
            println("Error syncing queued sales: ${e.message}")
            e.printStackTrace()
            false
        }
    }


    private fun createSqlDriver(databasePath: String): SqlDriver {
        val file = File(databasePath)
        val driver = JdbcSqliteDriver("jdbc:sqlite:${file.absolutePath}")

        if (!file.exists()) {
            AlmasaDatabase.Schema.create(driver)
        }

        return driver
    }
}