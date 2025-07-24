package io.yavero.almasasuite.pos.viewmodel

import io.yavero.almasasuite.model.JewelryProduct
import io.yavero.almasasuite.model.JewelryProductRequest
import io.yavero.almasasuite.model.JewelryType
import io.yavero.almasasuite.model.Product
import io.yavero.almasasuite.pos.repository.JewelryProductRepository
import io.yavero.almasasuite.pos.service.JewelryProductService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class JewelryPosUiState(

    val products: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,


    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false,


    val isManagerMode: Boolean = false,
    val isShowingPinDialog: Boolean = false,
    val currentTab: PosTab = PosTab.SALES,


    val selectedProduct: Product? = null,
    val isShowingProductForm: Boolean = false,
    val isShowingStockAdjustmentDialog: Boolean = false,


    val jewelryProducts: List<JewelryProduct> = emptyList(),
    val selectedJewelryProduct: JewelryProduct? = null,
    val isShowingJewelryProductForm: Boolean = false,
    val isJewelryMode: Boolean = false
)


class JewelryPosViewModel {

    private val scope = CoroutineScope(Dispatchers.Main)


    private val jewelryProductService = JewelryProductService()
    private val jewelryProductRepository = JewelryProductRepository()


    private val _uiState = MutableStateFlow(JewelryPosUiState())
    val uiState: StateFlow<JewelryPosUiState> = _uiState.asStateFlow()

    init {

        loadJewelryProducts()
    }


    private fun loadJewelryProducts() {
        scope.launch {
            val result = jewelryProductService.loadProducts()
            result.fold(
                onSuccess = { jewelryProducts ->

                    val products = jewelryProductRepository.convertToProducts(jewelryProducts)


                    _uiState.update {
                        it.copy(
                            jewelryProducts = jewelryProducts,
                            products = products,
                            isLoading = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }


    fun showJewelryProductForm(product: JewelryProduct? = null) {
        _uiState.update {
            it.copy(
                selectedJewelryProduct = product,
                isShowingJewelryProductForm = true
            )
        }
    }


    fun hideJewelryProductForm() {
        _uiState.update {
            it.copy(
                isShowingJewelryProductForm = false
            )
        }
    }


    fun createJewelryProduct(
        sku: String? = null,
        imageUrl: String? = null,
        type: JewelryType,
        karat: Int,
        weightGrams: Double,
        designFee: Double,
        purchasePrice: Double,
        quantityInStock: Int = 0
    ) {
        scope.launch {
            val result = jewelryProductService.createProduct(
                sku, imageUrl, type, karat, weightGrams, designFee, purchasePrice, quantityInStock
            )

            result.fold(
                onSuccess = { createdProduct ->

                    val regularProduct = jewelryProductRepository.convertToProduct(createdProduct)


                    _uiState.update {
                        it.copy(
                            products = _uiState.value.products + regularProduct,
                            isShowingJewelryProductForm = false,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }


    fun updateJewelryProduct(
        id: String,
        sku: String,
        imageUrl: String? = null,
        type: JewelryType,
        karat: Int,
        weightGrams: Double,
        designFee: Double,
        purchasePrice: Double,
        quantityInStock: Int
    ) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {

                val jewelryProductRequest = JewelryProductRequest(
                    sku = sku,
                    imageUrl = imageUrl,
                    type = type,
                    karat = karat,
                    weightGrams = weightGrams,
                    designFee = designFee,
                    purchasePrice = purchasePrice,
                    quantityInStock = quantityInStock
                )


                val updatedJewelryProduct = jewelryProductRepository.updateJewelryProduct(id, jewelryProductRequest)

                if (updatedJewelryProduct != null) {

                    val regularProduct = jewelryProductRepository.convertToProduct(updatedJewelryProduct)


                    val updatedJewelryProducts = _uiState.value.jewelryProducts.toMutableList()
                    val jewelryIndex = updatedJewelryProducts.indexOfFirst { it.id == id }

                    if (jewelryIndex != -1) {
                        updatedJewelryProducts[jewelryIndex] = updatedJewelryProduct
                    }

                    val updatedProducts = _uiState.value.products.toMutableList()
                    val regularIndex = updatedProducts.indexOfFirst { it.id == id }

                    if (regularIndex != -1) {
                        updatedProducts[regularIndex] = regularProduct
                    }


                    _uiState.update {
                        it.copy(
                            jewelryProducts = updatedJewelryProducts,
                            products = updatedProducts,
                            isLoading = false,
                            isShowingJewelryProductForm = false,
                            selectedJewelryProduct = null
                        )
                    }
                } else {

                    _uiState.update {
                        it.copy(
                            error = "Failed to update jewelry product. Please try again.",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }


    fun adjustJewelryProductStock(id: String, delta: Int) {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {

                val updatedJewelryProduct = jewelryProductRepository.adjustJewelryProductStock(id, delta)

                if (updatedJewelryProduct != null) {

                    val regularProduct = jewelryProductRepository.convertToProduct(updatedJewelryProduct)


                    val updatedJewelryProducts = _uiState.value.jewelryProducts.toMutableList()
                    val jewelryIndex = updatedJewelryProducts.indexOfFirst { it.id == id }

                    if (jewelryIndex != -1) {
                        updatedJewelryProducts[jewelryIndex] = updatedJewelryProduct
                    }

                    val updatedProducts = _uiState.value.products.toMutableList()
                    val regularIndex = updatedProducts.indexOfFirst { it.id == id }

                    if (regularIndex != -1) {
                        updatedProducts[regularIndex] = regularProduct
                    }


                    _uiState.update {
                        it.copy(
                            jewelryProducts = updatedJewelryProducts,
                            products = updatedProducts,
                            isLoading = false,
                            isShowingStockAdjustmentDialog = false,
                            selectedJewelryProduct = null
                        )
                    }
                } else {

                    _uiState.update {
                        it.copy(
                            error = "Failed to adjust stock. Please try again.",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message,
                        isLoading = false
                    )
                }
            }
        }
    }


    fun deleteJewelryProduct(id: String) {
        scope.launch {
            val result = jewelryProductService.deleteProduct(id)

            result.fold(
                onSuccess = {

                    val updatedJewelryProducts = _uiState.value.jewelryProducts.filter { it.id != id }
                    val updatedProducts = _uiState.value.products.filter { it.id != id }


                    _uiState.update {
                        it.copy(
                            jewelryProducts = updatedJewelryProducts,
                            products = updatedProducts,
                            error = null
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message
                        )
                    }
                }
            )
        }
    }


    fun exportJewelryProductsToCsv() {
        scope.launch {
            try {
                val products = _uiState.value.jewelryProducts
                val csvContent = generateJewelryProductsCsv(products)


                saveCsvFile("jewelry_inventory_${System.currentTimeMillis()}.csv", csvContent)


                _uiState.update { it.copy(error = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to export CSV: ${e.message}")
                }
            }
        }
    }


    private fun generateJewelryProductsCsv(products: List<JewelryProduct>): String {
        val header = "SKU,Type,Karat,Weight (g),Design Fee,Purchase Price,Total Price,Quantity,Created At,Updated At"
        val rows = products.map { product ->
            "${product.sku}," +
            "${product.type.name}," +
            "${product.karat}," +
            "${product.weightGrams}," +
            "${product.designFee}," +
            "${product.purchasePrice}," +
            "${product.totalPrice}," +
            "${product.quantityInStock}," +
            "${formatTimestamp(product.createdAt)}," +
            "${formatTimestamp(product.updatedAt)}"
        }

        return listOf(header).plus(rows).joinToString("\n")
    }


    private fun formatTimestamp(timestamp: Long): String {
        return if (timestamp > 0) {
            java.time.Instant.ofEpochMilli(timestamp).toString()
        } else {
            ""
        }
    }


    private fun saveCsvFile(filename: String, content: String) {


        println("CSV Export: $filename")
        println(content)
    }


    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

}