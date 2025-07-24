package io.yavero.almasasuite.pos.ui.jewelry

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.model.JewelryProduct
import io.yavero.almasasuite.model.JewelryType
import io.yavero.almasasuite.pos.localization.*
import io.yavero.almasasuite.pos.ui.components.PhotoCaptureField
import io.yavero.almasasuite.pos.ui.components.PhotoData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JewelryProductFormDialog(
    product: JewelryProduct?,
    onDismiss: () -> Unit,
    onSave: (JewelryProduct, Boolean) -> Unit
) {
    val isNewProduct = product == null


    var selectedPhoto by remember {
        mutableStateOf<PhotoData?>(
            product?.imageUrl?.let { url ->
                PhotoData(
                    filePath = url,
                    fileName = url.substringAfterLast('/').ifEmpty { "existing_image.jpg" },
                    isFromCamera = false
                )
            }
        )
    }
    var selectedType by remember { mutableStateOf(product?.type ?: JewelryType.OTHER) }
    var karat by remember { mutableStateOf((product?.karat ?: 18).toString()) }
    var weightGrams by remember { mutableStateOf((product?.weightGrams ?: 0.0).toString()) }
    var designFee by remember { mutableStateOf((product?.designFee ?: 0.0).toString()) }
    var purchasePrice by remember { mutableStateOf((product?.purchasePrice ?: 0.0).toString()) }
    var quantityInStock by remember { mutableStateOf((product?.quantityInStock ?: 0).toString()) }


    var expanded by remember { mutableStateOf(false) }


    val totalPrice = (designFee.toDoubleOrNull() ?: 0.0) + (purchasePrice.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNewProduct) getString(StringResources.ADD_JEWELRY_PRODUCT) else getString(StringResources.EDIT_JEWELRY_PRODUCT)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                PhotoCaptureField(
                    currentPhoto = selectedPhoto,
                    onPhotoSelected = { selectedPhoto = it },
                    modifier = Modifier.fillMaxWidth()
                )


                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(getString(StringResources.JEWELRY_TYPE)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        JewelryType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }


                OutlinedTextField(
                    value = karat,
                    onValueChange = { karat = it },
                    label = { Text(getString(StringResources.KARAT)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = weightGrams,
                    onValueChange = { weightGrams = it },
                    label = { Text(getString(StringResources.WEIGHT_GRAMS)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = designFee,
                    onValueChange = { designFee = it },
                    label = { Text(getString(StringResources.DESIGN_FEE)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text(getString(StringResources.PURCHASE_PRICE)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = totalPrice.toString(),
                    onValueChange = {},
                    label = { Text(getString(StringResources.TOTAL_PRICE)) },
                    singleLine = true,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )


            }
        },
        confirmButton = {
            Button(
                onClick = {

                    val karatInt = karat.toIntOrNull() ?: 0
                    val weightDouble = weightGrams.toDoubleOrNull() ?: 0.0
                    val designFeeDouble = designFee.toDoubleOrNull() ?: 0.0
                    val purchasePriceDouble = purchasePrice.toDoubleOrNull() ?: 0.0
                    val quantityInt = quantityInStock.toIntOrNull() ?: 0


                    val updatedProduct = JewelryProduct(
                        id = product?.id ?: java.util.UUID.randomUUID().toString(),
                        sku = product?.sku ?: "JWL-${java.util.UUID.randomUUID().toString().take(8).uppercase()}",
                        imageUrl = selectedPhoto?.filePath,
                        type = selectedType,
                        karat = karatInt,
                        weightGrams = weightDouble,
                        designFee = designFeeDouble,
                        purchasePrice = purchasePriceDouble,
                        quantityInStock = quantityInt,
                        createdAt = product?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )

                    onSave(updatedProduct, isNewProduct)
                }
            ) {
                Text(getString(StringResources.SAVE))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(getString(StringResources.CANCEL))
            }
        }
    )
}