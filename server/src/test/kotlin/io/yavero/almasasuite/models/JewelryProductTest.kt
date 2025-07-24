package io.yavero.almasasuite.models

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class JewelryProductTest {


    private fun validateProduct(product: ProductRequest): Pair<Boolean, String?> {

        if (product.karat <= 0) {
            return Pair(false, "Karat must be positive")
        }


        if (product.weightGrams <= 0) {
            return Pair(false, "Weight must be positive")
        }


        return Pair(true, null)
    }


    private fun generateSku(type: ProductType, karat: Int, weightGrams: Double): String {
        val typePrefix = when (type) {
            ProductType.RING -> "R"
            ProductType.BRACELET -> "B"
            ProductType.NECKLACE -> "N"
            ProductType.EARRING -> "E"
            ProductType.OTHER -> "O"
        }

        return "$typePrefix-$karat-$weightGrams-ABCDEF"
    }

    @Test
    fun `test product validation - zero weight should be rejected`() {

        val productRequest = ProductRequest(
            sku = "TEST-ZERO-WEIGHT",
            type = ProductType.RING,
            karat = 18,
            weightGrams = 0.0,
            designFee = 50.0,
            purchasePrice = 100.0,
            quantityInStock = 1
        )


        val (isValid, errorMessage) = validateProduct(productRequest)


        assertFalse(isValid, "Product with zero weight should be rejected")
        assertEquals("Weight must be positive", errorMessage)
    }

    @Test
    fun `test product validation - negative karat should be rejected`() {

        val productRequest = ProductRequest(
            sku = "TEST-NEG-KARAT",
            type = ProductType.RING,
            karat = -5,
            weightGrams = 5.5,
            designFee = 50.0,
            purchasePrice = 100.0,
            quantityInStock = 1
        )


        val (isValid, errorMessage) = validateProduct(productRequest)


        assertFalse(isValid, "Product with negative karat should be rejected")
        assertEquals("Karat must be positive", errorMessage)
    }

    @Test
    fun `test product validation - valid product should be accepted`() {

        val productRequest = ProductRequest(
            sku = "TEST-VALID-PRODUCT",
            type = ProductType.RING,
            karat = 18,
            weightGrams = 5.5,
            designFee = 50.0,
            purchasePrice = 100.0,
            quantityInStock = 1
        )


        val (isValid, errorMessage) = validateProduct(productRequest)


        assertTrue(isValid, "Valid product should be accepted")
        assertEquals(null, errorMessage)
    }

    @Test
    fun `test SKU auto-generation format`() {

        val sku = generateSku(
            type = ProductType.RING,
            karat = 18,
            weightGrams = 5.5
        )


        assertEquals("R-18-5.5-ABCDEF", sku)


        assertEquals("B-24-10.0-ABCDEF", generateSku(ProductType.BRACELET, 24, 10.0))
        assertEquals("N-22-15.5-ABCDEF", generateSku(ProductType.NECKLACE, 22, 15.5))
        assertEquals("E-14-2.5-ABCDEF", generateSku(ProductType.EARRING, 14, 2.5))
        assertEquals("O-9-8.75-ABCDEF", generateSku(ProductType.OTHER, 9, 8.75))
    }

    @Test
    fun `test profit calculation`() {

        val salePrice = 1000.0
        val purchasePrice = 600.0
        val designFee = 150.0
        val quantity = 1


        val profit = salePrice - purchasePrice - designFee


        assertEquals(250.0, profit)


        val quantity2 = 2
        val totalSalePrice = salePrice * quantity2
        val totalPurchasePrice = purchasePrice * quantity2
        val totalDesignFee = designFee * quantity2
        val totalProfit = totalSalePrice - totalPurchasePrice - totalDesignFee


        assertEquals(500.0, totalProfit)
    }
}