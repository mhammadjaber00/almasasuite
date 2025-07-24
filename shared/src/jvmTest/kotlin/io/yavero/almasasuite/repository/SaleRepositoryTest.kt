package io.yavero.almasasuite.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.model.PaymentMethod
import io.yavero.almasasuite.model.Product
import io.yavero.almasasuite.model.Sale
import io.yavero.almasasuite.model.SaleItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.UUID

class SaleRepositoryTest : FunSpec({


    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AlmasaDatabase.Schema.create(driver)
    val database = AlmasaDatabase(driver)


    val productRepository = ProductRepositoryImpl(database)
    val stockMutationRepository = StockMutationRepositoryImpl(database)
    val saleRepository = SaleRepositoryImpl(database, stockMutationRepository)


    val product1 = Product(
        id = UUID.randomUUID().toString(),
        sku = "RING-001",
        name = "Diamond Ring",
        stone = "Diamond",
        carat = 1.5,
        weight = 5.0,
        price = 2500.0,
        quantityInStock = 10
    )

    val product2 = Product(
        id = UUID.randomUUID().toString(),
        sku = "NECKLACE-001",
        name = "Ruby Necklace",
        stone = "Ruby",
        carat = 2.0,
        weight = 8.0,
        price = 3500.0,
        quantityInStock = 5
    )


    val saleId = UUID.randomUUID().toString()
    val saleDate = Clock.System.now()

    fun createSampleSale(): Sale {
        val saleItems = listOf(
            SaleItem(
                productId = product1.id,
                quantity = 1,
                unitPrice = product1.price,
                subtotal = product1.price
            ),
            SaleItem(
                productId = product2.id,
                quantity = 2,
                unitPrice = product2.price,
                subtotal = product2.price * 2
            )
        )

        return Sale(
            id = saleId,
            date = saleDate,
            items = saleItems,
            total = saleItems.sumOf { it.subtotal },
            paymentMethod = PaymentMethod.CREDIT_CARD
        )
    }

    beforeTest {

        runTest {
            productRepository.insertProduct(product1)
            productRepository.insertProduct(product2)
        }
    }

    test("Create sale should succeed") {
        runTest {
            val sale = createSampleSale()
            val result = saleRepository.createSale(sale)
            result shouldBe true
        }
    }

    test("Get sale by ID should return the correct sale") {
        runTest {
            val sale = createSampleSale()
            saleRepository.createSale(sale)

            val retrievedSale = saleRepository.getSaleById(sale.id)
            retrievedSale shouldNotBe null
            retrievedSale?.id shouldBe sale.id
            retrievedSale?.total shouldBe sale.total
            retrievedSale?.paymentMethod shouldBe sale.paymentMethod
        }
    }

    test("Get sale with items should return the correct sale with items") {
        runTest {
            val sale = createSampleSale()
            saleRepository.createSale(sale)

            val retrievedSale = saleRepository.getSaleWithItems(sale.id)
            retrievedSale shouldNotBe null
            retrievedSale?.id shouldBe sale.id
            retrievedSale?.items?.size shouldBe 2
            retrievedSale?.items?.sumOf { it.subtotal } shouldBe sale.total
        }
    }

    test("Get sales by date range should return correct sales") {
        runTest {
            val sale = createSampleSale()
            saleRepository.createSale(sale)


            val startDate = Instant.fromEpochMilliseconds(saleDate.toEpochMilliseconds() - 3600000)
            val endDate = Instant.fromEpochMilliseconds(saleDate.toEpochMilliseconds() + 3600000)

            val sales = saleRepository.getSalesByDateRange(startDate, endDate)
            sales.size shouldBe 1
            sales[0].id shouldBe sale.id


            val pastStartDate = Instant.fromEpochMilliseconds(saleDate.toEpochMilliseconds() - 7200000)
            val pastEndDate = Instant.fromEpochMilliseconds(saleDate.toEpochMilliseconds() - 3600000)

            val pastSales = saleRepository.getSalesByDateRange(pastStartDate, pastEndDate)
            pastSales.size shouldBe 0
        }
    }

    test("Get unsynced sales should return correct sales") {
        runTest {
            val sale = createSampleSale()
            saleRepository.createSale(sale)

            val unsyncedSales = saleRepository.getUnsyncedSales()
            unsyncedSales.size shouldBe 1
            unsyncedSales[0].id shouldBe sale.id
        }
    }

    test("Mark sale as synced should succeed") {
        runTest {
            val sale = createSampleSale()
            saleRepository.createSale(sale)


            val unsyncedSales = saleRepository.getUnsyncedSales()
            unsyncedSales.size shouldBe 1


            val result = saleRepository.markSaleAsSynced(unsyncedSales[0].id)
            result shouldBe true


            val remainingUnsyncedSales = saleRepository.getUnsyncedSales()
            remainingUnsyncedSales.size shouldBe 0
        }
    }

    test("Delete sale should succeed") {
        runTest {
            val sale = createSampleSale()
            saleRepository.createSale(sale)


            val retrievedSale = saleRepository.getSaleById(sale.id)
            retrievedSale shouldNotBe null


            val result = saleRepository.deleteSale(sale.id)
            result shouldBe true


            val deletedSale = saleRepository.getSaleById(sale.id)
            deletedSale shouldBe null
        }
    }

    test("Creating a sale should reduce product stock") {
        runTest {

            val initialProduct1 = productRepository.getProductById(product1.id)
            val initialProduct2 = productRepository.getProductById(product2.id)

            initialProduct1?.quantityInStock shouldBe 10
            initialProduct2?.quantityInStock shouldBe 5


            val sale = createSampleSale()
            saleRepository.createSale(sale)


            val updatedProduct1 = productRepository.getProductById(product1.id)
            val updatedProduct2 = productRepository.getProductById(product2.id)

            updatedProduct1?.quantityInStock shouldBe 9
            updatedProduct2?.quantityInStock shouldBe 3
        }
    }

    test("Creating a sale should create stock mutations") {
        runTest {

            val sale = createSampleSale()
            saleRepository.createSale(sale)


            val product1Mutations = stockMutationRepository.getStockMutationsByProductId(product1.id)
            val product2Mutations = stockMutationRepository.getStockMutationsByProductId(product2.id)

            product1Mutations.size shouldBe 1
            product1Mutations[0].delta shouldBe -1
            product1Mutations[0].reason.name shouldBe "SALE"

            product2Mutations.size shouldBe 1
            product2Mutations[0].delta shouldBe -2
            product2Mutations[0].reason.name shouldBe "SALE"
        }
    }

    test("Get all sales should return all sales") {
        runTest {

            val sale = createSampleSale()
            saleRepository.createSale(sale)


            val anotherSaleId = UUID.randomUUID().toString()
            val anotherSale = sale.copy(id = anotherSaleId)
            saleRepository.createSale(anotherSale)


            val allSales = saleRepository.getAllSales().first()
            allSales.size shouldBe 2
        }
    }
})