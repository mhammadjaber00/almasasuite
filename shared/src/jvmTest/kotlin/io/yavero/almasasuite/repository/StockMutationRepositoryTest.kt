package io.yavero.almasasuite.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.model.Product
import io.yavero.almasasuite.model.StockMutation
import io.yavero.almasasuite.model.StockMutationReason
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import java.util.UUID

class StockMutationRepositoryTest : FunSpec({


    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AlmasaDatabase.Schema.create(driver)
    val database = AlmasaDatabase(driver)


    val productRepository = ProductRepositoryImpl(database)
    val stockMutationRepository = StockMutationRepositoryImpl(database)


    val product = Product(
        id = UUID.randomUUID().toString(),
        sku = "RING-001",
        name = "Diamond Ring",
        stone = "Diamond",
        carat = 1.5,
        weight = 5.0,
        price = 2500.0,
        quantityInStock = 10
    )

    beforeTest {

        runTest {
            productRepository.insertProduct(product)
        }
    }

    test("Create stock mutation should succeed") {
        runTest {
            val result = stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )
            result shouldBe true
        }
    }

    test("Get stock mutations by product ID should return correct mutations") {
        runTest {

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )


            val mutations = stockMutationRepository.getStockMutationsByProductId(product.id)
            mutations.size shouldBe 1
            mutations[0].productId shouldBe product.id
            mutations[0].delta shouldBe -2
            mutations[0].reason shouldBe StockMutationReason.SALE
        }
    }

    test("Get stock mutations by reason should return correct mutations") {
        runTest {

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = 5,
                reason = StockMutationReason.PURCHASE.name,
                timestamp = Clock.System.now()
            )


            val saleMutations = stockMutationRepository.getStockMutationsByReason(StockMutationReason.SALE)
            saleMutations.size shouldBe 1
            saleMutations[0].reason shouldBe StockMutationReason.SALE

            val purchaseMutations = stockMutationRepository.getStockMutationsByReason(StockMutationReason.PURCHASE)
            purchaseMutations.size shouldBe 1
            purchaseMutations[0].reason shouldBe StockMutationReason.PURCHASE
        }
    }

    test("Get unsynced stock mutations should return correct mutations") {
        runTest {

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = 5,
                reason = StockMutationReason.PURCHASE.name,
                timestamp = Clock.System.now()
            )


            val unsyncedMutations = stockMutationRepository.getUnsyncedStockMutations()
            unsyncedMutations.size shouldBe 2
        }
    }

    test("Mark stock mutation as synced should succeed") {
        runTest {

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )


            val unsyncedMutations = stockMutationRepository.getUnsyncedStockMutations()
            unsyncedMutations.size shouldBe 1


            val result = stockMutationRepository.markStockMutationAsSynced(unsyncedMutations[0].id)
            result shouldBe true


            val remainingUnsyncedMutations = stockMutationRepository.getUnsyncedStockMutations()
            remainingUnsyncedMutations.size shouldBe 0
        }
    }

    test("Delete stock mutation should succeed") {
        runTest {

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )


            val mutations = stockMutationRepository.getStockMutationsByProductId(product.id)
            mutations.size shouldBe 1


            val result = stockMutationRepository.deleteStockMutation(mutations[0].id)
            result shouldBe true


            val remainingMutations = stockMutationRepository.getStockMutationsByProductId(product.id)
            remainingMutations.size shouldBe 0
        }
    }

    test("Get all stock mutations should return all mutations") {
        runTest {

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = -2,
                reason = StockMutationReason.SALE.name,
                timestamp = Clock.System.now()
            )

            stockMutationRepository.createStockMutation(
                productId = product.id,
                delta = 5,
                reason = StockMutationReason.PURCHASE.name,
                timestamp = Clock.System.now()
            )


            val allMutations = stockMutationRepository.getAllStockMutations().first()
            allMutations.size shouldBe 2
        }
    }
})