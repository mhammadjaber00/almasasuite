package io.yavero.almasasuite.pos.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import io.yavero.almasasuite.model.AuthenticatedUser
import io.yavero.almasasuite.pos.localization.StringResources
import io.yavero.almasasuite.pos.localization.getString
import io.yavero.almasasuite.pos.viewmodel.JewelryPosViewModel
import io.yavero.almasasuite.pos.ui.jewelry.JewelryProductFormDialog


@Composable
fun StandaloneInventoryScreen(
    user: AuthenticatedUser
) {
    val viewModel = remember { JewelryPosViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    val searchFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .onKeyEvent { keyEvent ->

                if (keyEvent.type == KeyEventType.KeyDown) {
                    when {

                        (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && keyEvent.key == Key.F -> {
                            coroutineScope.launch {
                                searchFocusRequester.requestFocus()
                            }
                            true
                        }
                        else -> false
                    }
                } else false
            }
    ) {
        InventoryPanel(
            products = uiState.jewelryProducts,
            isLoading = uiState.isLoading,
            onAddProduct = { viewModel.showJewelryProductForm() },
            onEditProduct = { viewModel.showJewelryProductForm(it) },
            onDeleteProduct = { product -> viewModel.deleteJewelryProduct(product.id) },
            onAdjustStock = { product, delta, reason ->
                viewModel.adjustJewelryProductStock(product.id, delta)
            },
            onExportCsv = { viewModel.exportJewelryProductsToCsv() },
            searchFocusRequester = searchFocusRequester
        )


        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text(getString(StringResources.ERROR)) },
                text = { Text(uiState.error!!) },
                confirmButton = {
                    Button(onClick = { viewModel.clearError() }) {
                        Text(getString(StringResources.OK))
                    }
                }
            )
        }


        if (uiState.isShowingJewelryProductForm) {
            JewelryProductFormDialog(
                product = uiState.selectedJewelryProduct,
                onDismiss = { viewModel.hideJewelryProductForm() },
                onSave = { product, isNewProduct ->
                    if (isNewProduct) {
                        viewModel.createJewelryProduct(
                            sku = product.sku,
                            imageUrl = product.imageUrl,
                            type = product.type,
                            karat = product.karat,
                            weightGrams = product.weightGrams,
                            designFee = product.designFee,
                            purchasePrice = product.purchasePrice,
                            quantityInStock = product.quantityInStock
                        )
                    } else {
                        viewModel.updateJewelryProduct(
                            id = product.id,
                            sku = product.sku,
                            imageUrl = product.imageUrl,
                            type = product.type,
                            karat = product.karat,
                            weightGrams = product.weightGrams,
                            designFee = product.designFee,
                            purchasePrice = product.purchasePrice,
                            quantityInStock = product.quantityInStock
                        )
                    }
                }
            )
        }
    }
}