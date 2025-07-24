package io.yavero.almasasuite.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.model.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.UUID

class ProductRepositoryTest : FunSpec({


    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    AlmasaDatabase.Schema.create(driver)
    val database = AlmasaDatabase(driver)


    val repository = ProductRepositoryImpl(database)


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

    test("Insert product should succeed") {
        runTest {
            val result = repository.insertProduct(product1)
            result shouldBe true
        }
    }

    test("Get product by ID should return the correct product") {
        runTest {
            repository.insertProduct(product1)
            val retrievedProduct = repository.getProductById(product1.id)
            retrievedProduct shouldNotBe null
            retrievedProduct?.id shouldBe product1.id
            retrievedProduct?.name shouldBe product1.name
        }
    }

    test("Get product by SKU should return the correct product") {
        runTest {
            repository.insertProduct(product1)
            val retrievedProduct = repository.getProductBySku(product1.sku)
            retrievedProduct shouldNotBe null
            retrievedProduct?.sku shouldBe product1.sku
            retrievedProduct?.name shouldBe product1.name
        }
    }

    test("Update product should succeed") {
        runTest {
            repository.insertProduct(product1)

            val updatedProduct = product1.copy(
                name = "Diamond Ring (Updated)",
                price = 2700.0,
                quantityInStock = 8
            )

            val result = repository.updateProduct(updatedProduct)
            result shouldBe true

            val retrievedProduct = repository.getProductById(product1.id)
            retrievedProduct shouldNotBe null
            retrievedProduct?.name shouldBe "Diamond Ring (Updated)"
            retrievedProduct?.price shouldBe 2700.0
            retrievedProduct?.quantityInStock shouldBe 8
        }
    }

    test("Update product stock should succeed") {
        runTest {
            repository.insertProduct(product1)

            val result = repository.updateProductStock(product1.id, 15)
            result shouldBe true

            val retrievedProduct = repository.getProductById(product1.id)
            retrievedProduct shouldNotBe null
            retrievedProduct?.quantityInStock shouldBe 15
        }
    }

    test("Search products by name should return matching products") {
        runTest {
            repository.insertProduct(product1)
            repository.insertProduct(product2)

            val results = repository.searchProductsByName("Diamond")
            results.size shouldBe 1
            results[0].name shouldBe "Diamond Ring"

            val results2 = repository.searchProductsByName("Ruby")
            results2.size shouldBe 1
            results2[0].name shouldBe "Ruby Necklace"

            val results3 = repository.searchProductsByName("Ring")
            results3.size shouldBe 1
            results3[0].name shouldBe "Diamond Ring"
        }
    }

    test("Get all products should return all products") {
        runTest {
            repository.insertProduct(product1)
            repository.insertProduct(product2)

            val allProducts = repository.getAllProducts().first()
            allProducts.size shouldBe 2
        }
    }

    test("Delete product should succeed") {
        runTest {
            repository.insertProduct(product1)

            val result = repository.deleteProduct(product1.id)
            result shouldBe true

            val retrievedProduct = repository.getProductById(product1.id)
            retrievedProduct shouldBe null
        }
    }
})