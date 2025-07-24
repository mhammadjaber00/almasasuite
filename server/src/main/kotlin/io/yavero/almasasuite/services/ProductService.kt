package io.yavero.almasasuite.services

import io.yavero.almasasuite.models.*
import io.yavero.almasasuite.plugins.dbQuery
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*


class ProductService {


    fun generateSku(type: ProductType, karat: Int, weightGrams: Double): String {
        val typePrefix = when (type) {
            ProductType.RING -> "R"
            ProductType.BRACELET -> "B"
            ProductType.NECKLACE -> "N"
            ProductType.EARRING -> "E"
            ProductType.OTHER -> "O"
        }


        val karatFormatted = karat.toString().padStart(2, '0')


        val weightFormatted = (weightGrams * 10).toInt().toString().padStart(3, '0')


        val sequence = (1..999).random().toString().padStart(3, '0')

        return "ALM$typePrefix$karatFormatted$weightFormatted$sequence"
    }


    fun isValidBarcodeFormat(sku: String): Boolean {

        val pattern = Regex("^ALM[RBNOE]\\d{2}\\d{3}\\d{3}$")
        return pattern.matches(sku)
    }


    fun parseSkuInfo(sku: String): SkuInfo? {
        if (!isValidBarcodeFormat(sku)) return null

        try {
            val type = when (sku[3]) {
                'R' -> ProductType.RING
                'B' -> ProductType.BRACELET
                'N' -> ProductType.NECKLACE
                'E' -> ProductType.EARRING
                'O' -> ProductType.OTHER
                else -> return null
            }

            val karat = sku.substring(4, 6).toInt()
            val weight = sku.substring(6, 9).toInt() / 10.0
            val sequence = sku.substring(9, 12).toInt()

            return SkuInfo(type, karat, weight, sequence)
        } catch (e: Exception) {
            return null
        }
    }


    fun validateProductRequest(request: ProductRequest): String? {
        return when {
            request.karat <= 0 -> "Karat must be positive"
            request.weightGrams <= 0 -> "Weight must be positive"
            request.designFee < 0 -> "Design fee cannot be negative"
            request.purchasePrice < 0 -> "Purchase price cannot be negative"
            request.quantityInStock < 0 -> "Quantity cannot be negative"
            else -> null
        }
    }


    private fun mapRowToProduct(row: ResultRow): Product {
        return Product(
            id = row[Products.id],
            sku = row[Products.sku],
            imageUrl = row[Products.imageUrl],
            type = ProductType.fromString(row[Products.type]),
            karat = row[Products.karat],
            weightGrams = row[Products.weightGrams].toDouble(),
            designFee = row[Products.designFee].toDouble(),
            purchasePrice = row[Products.purchasePrice].toDouble(),
            quantityInStock = row[Products.quantityInStock],
            createdAt = row[Products.createdAt],
            updatedAt = row[Products.updatedAt]
        )
    }


    suspend fun getAllProducts(): List<ProductResponse> = dbQuery {
        Products.selectAll()
            .orderBy(Products.type to SortOrder.ASC, Products.karat to SortOrder.ASC, Products.weightGrams to SortOrder.ASC)
            .map { mapRowToProduct(it).toResponse() }
    }


    suspend fun getProductById(id: String): ProductResponse? = dbQuery {
        Products.selectAll().where { Products.id eq id }
            .map { mapRowToProduct(it).toResponse() }
            .singleOrNull()
    }


    suspend fun skuExists(sku: String): Boolean = dbQuery {
        Products.selectAll().where { Products.sku eq sku }.count() > 0
    }


    suspend fun skuExistsForOtherProduct(sku: String, excludeId: String): Boolean = dbQuery {
        Products.selectAll().where { (Products.sku eq sku) and (Products.id neq excludeId) }.count() > 0
    }


    suspend fun productExists(id: String): Boolean = dbQuery {
        Products.selectAll().where { Products.id eq id }.count() > 0
    }


    suspend fun createProduct(request: ProductRequest): ProductResponse = dbQuery {
        val productId = UUID.randomUUID().toString()
        val now = Clock.System.now().toEpochMilliseconds()


        val sku = if (request.sku.isNullOrBlank()) {
            generateSku(request.type, request.karat, request.weightGrams)
        } else {
            request.sku
        }

        Products.insert {
            it[id] = productId
            it[Products.sku] = sku
            it[imageUrl] = request.imageUrl
            it[type] = ProductType.toDbValue(request.type)
            it[karat] = request.karat
            it[weightGrams] = request.weightGrams.toBigDecimal()
            it[designFee] = request.designFee.toBigDecimal()
            it[purchasePrice] = request.purchasePrice.toBigDecimal()
            it[quantityInStock] = request.quantityInStock
            it[createdAt] = now
            it[updatedAt] = now
        }


        Product(
            id = productId,
            sku = sku!!,
            imageUrl = request.imageUrl,
            type = request.type,
            karat = request.karat,
            weightGrams = request.weightGrams,
            designFee = request.designFee,
            purchasePrice = request.purchasePrice,
            quantityInStock = request.quantityInStock,
            createdAt = now,
            updatedAt = now
        ).toResponse()
    }


    suspend fun updateProduct(id: String, request: ProductRequest): ProductResponse? = dbQuery {
        val now = Clock.System.now().toEpochMilliseconds()


        val sku = if (request.sku.isNullOrBlank()) {
            generateSku(request.type, request.karat, request.weightGrams)
        } else {
            request.sku
        }

        val updateCount = Products.update({ Products.id eq id }) {
            it[Products.sku] = sku!!
            it[imageUrl] = request.imageUrl
            it[type] = ProductType.toDbValue(request.type)
            it[karat] = request.karat
            it[weightGrams] = request.weightGrams.toBigDecimal()
            it[designFee] = request.designFee.toBigDecimal()
            it[purchasePrice] = request.purchasePrice.toBigDecimal()
            it[quantityInStock] = request.quantityInStock
            it[updatedAt] = now
        }

        if (updateCount > 0) {
            Product(
                id = id,
                sku = sku!!,
                imageUrl = request.imageUrl,
                type = request.type,
                karat = request.karat,
                weightGrams = request.weightGrams,
                designFee = request.designFee,
                purchasePrice = request.purchasePrice,
                quantityInStock = request.quantityInStock,
                createdAt = 0,
                updatedAt = now
            ).toResponse()
        } else {
            null
        }
    }


    suspend fun deleteProduct(id: String): Boolean = dbQuery {
        Products.deleteWhere { Products.id eq id } > 0
    }


    suspend fun updateStock(id: String, newQuantity: Int): Boolean = dbQuery {
        val now = Clock.System.now().toEpochMilliseconds()
        Products.update({ Products.id eq id }) {
            it[quantityInStock] = newQuantity
            it[updatedAt] = now
        } > 0
    }
}