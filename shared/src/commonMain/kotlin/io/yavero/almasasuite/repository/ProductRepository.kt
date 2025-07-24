package io.yavero.almasasuite.repository

import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.db.Product as DbProduct
import io.yavero.almasasuite.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock


sealed class ProductRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ProductNotFound(id: String) : ProductRepositoryException("Product with ID '$id' not found")
    class DuplicateSku(sku: String) : ProductRepositoryException("Product with SKU '$sku' already exists")
    class InvalidProductData(field: String, value: Any?) : ProductRepositoryException("Invalid $field: $value")
    class DatabaseError(operation: String, cause: Throwable) : ProductRepositoryException("Database error during $operation", cause)
    class ValidationError(message: String) : ProductRepositoryException("Validation error: $message")
}


interface ProductRepository {


    fun getAllProducts(): Flow<List<Product>>


    suspend fun getProductById(id: String): Product?


    suspend fun getProductBySku(sku: String): Product?


    suspend fun searchProductsByName(query: String): List<Product>


    suspend fun insertProduct(product: Product): Boolean


    suspend fun updateProduct(product: Product): Boolean


    suspend fun updateProductStock(id: String, newQuantity: Int): Boolean


    suspend fun deleteProduct(id: String): Boolean
}


class ProductRepositoryImpl(
    private val database: AlmasaDatabase
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return flow {
            val products = database.productQueries
                .getAllProducts()
                .executeAsList()
                .map { it.toProduct() }
            emit(products)
        }
    }

    override suspend fun getProductById(id: String): Product? = database.productQueries
        .getProductById(id)
        .executeAsOneOrNull()
        ?.toProduct()

    override suspend fun getProductBySku(sku: String): Product? = database.productQueries
        .getProductBySku(sku)
        .executeAsOneOrNull()
        ?.toProduct()

    override suspend fun searchProductsByName(query: String): List<Product> = database.productQueries
        .searchProductsByName(query)
        .executeAsList()
        .map { it.toProduct() }

    override suspend fun insertProduct(product: Product): Boolean {
        return try {

            validateProductData(product)


            val existingProduct = getProductBySku(product.sku)
            if (existingProduct != null) {
                throw ProductRepositoryException.DuplicateSku(product.sku)
            }

            val now = Clock.System.now().toEpochMilliseconds()
            database.productQueries.insertProduct(
                id = product.id,
                sku = product.sku,
                name = product.name,
                stone = product.stone,
                carat = product.carat,
                weight = product.weight,
                price = product.price,
                quantity_in_stock = product.quantityInStock.toLong(),
                created_at = now,
                updated_at = now
            )
            true
        } catch (e: ProductRepositoryException) {
            throw e
        } catch (e: Exception) {
            throw ProductRepositoryException.DatabaseError("product insertion", e)
        }
    }

    override suspend fun updateProduct(product: Product): Boolean {
        return try {

            validateProductData(product)


            val existingProduct = getProductById(product.id)
            if (existingProduct == null) {
                throw ProductRepositoryException.ProductNotFound(product.id)
            }

            val now = Clock.System.now().toEpochMilliseconds()
            database.productQueries.updateProduct(
                sku = product.sku,
                name = product.name,
                stone = product.stone,
                carat = product.carat,
                weight = product.weight,
                price = product.price,
                quantity_in_stock = product.quantityInStock.toLong(),
                updated_at = now,
                id = product.id
            )
            true
        } catch (e: ProductRepositoryException) {
            throw e
        } catch (e: Exception) {
            throw ProductRepositoryException.DatabaseError("product update", e)
        }
    }

    override suspend fun updateProductStock(id: String, newQuantity: Int): Boolean {
        return try {

            if (newQuantity < 0) {
                throw ProductRepositoryException.InvalidProductData("newQuantity", newQuantity)
            }


            val existingProduct = getProductById(id)
            if (existingProduct == null) {
                throw ProductRepositoryException.ProductNotFound(id)
            }

            val now = Clock.System.now().toEpochMilliseconds()
            database.productQueries.updateProductStock(
                quantity_in_stock = newQuantity.toLong(),
                updated_at = now,
                id = id
            )
            true
        } catch (e: ProductRepositoryException) {
            throw e
        } catch (e: Exception) {
            throw ProductRepositoryException.DatabaseError("stock update", e)
        }
    }

    override suspend fun deleteProduct(id: String): Boolean {
        return try {

            if (id.isBlank()) {
                throw ProductRepositoryException.InvalidProductData("id", id)
            }


            val existingProduct = getProductById(id)
            if (existingProduct == null) {
                throw ProductRepositoryException.ProductNotFound(id)
            }

            database.productQueries.deleteProduct(id)
            true
        } catch (e: ProductRepositoryException) {
            throw e
        } catch (e: Exception) {
            throw ProductRepositoryException.DatabaseError("product deletion", e)
        }
    }


    private fun validateProductData(product: Product) {
        if (product.id.isBlank()) {
            throw ProductRepositoryException.InvalidProductData("id", product.id)
        }
        if (product.sku.isBlank()) {
            throw ProductRepositoryException.InvalidProductData("sku", product.sku)
        }
        if (product.name.isBlank()) {
            throw ProductRepositoryException.InvalidProductData("name", product.name)
        }
        if (product.carat < 0) {
            throw ProductRepositoryException.InvalidProductData("carat", product.carat)
        }
        if (product.weight < 0) {
            throw ProductRepositoryException.InvalidProductData("weight", product.weight)
        }
        if (product.price < 0) {
            throw ProductRepositoryException.InvalidProductData("price", product.price)
        }
        if (product.quantityInStock < 0) {
            throw ProductRepositoryException.InvalidProductData("quantityInStock", product.quantityInStock)
        }
    }


    private fun DbProduct.toProduct(): Product {
        return Product(
            id = id,
            sku = sku,
            name = name,
            stone = stone,
            carat = carat,
            weight = weight,
            price = price,
            quantityInStock = quantity_in_stock.toInt()
        )
    }
}