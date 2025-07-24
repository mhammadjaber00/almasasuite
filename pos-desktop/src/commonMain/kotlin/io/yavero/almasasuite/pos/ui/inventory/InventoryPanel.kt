package io.yavero.almasasuite.pos.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.model.JewelryProduct
import io.yavero.almasasuite.model.JewelryType
import io.yavero.almasasuite.pos.localization.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryPanel(
    products: List<JewelryProduct>,
    isLoading: Boolean,
    onAddProduct: () -> Unit,
    onEditProduct: (JewelryProduct) -> Unit,
    onDeleteProduct: (JewelryProduct) -> Unit,
    onAdjustStock: (JewelryProduct, Int, String) -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
    searchFocusRequester: FocusRequester? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<JewelryType?>(null) }
    var showLowStockOnly by remember { mutableStateOf(false) }
    var showStockAdjustmentDialog by remember { mutableStateOf<JewelryProduct?>(null) }


    val filteredProducts = remember(products, searchQuery, selectedCategory, showLowStockOnly) {
        products.filter { product ->
            val matchesSearch = searchQuery.isEmpty() ||
                product.sku.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || product.type == selectedCategory
            val matchesStock = !showLowStockOnly || product.quantityInStock <= 5
            matchesSearch && matchesCategory && matchesStock
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getString(StringResources.INVENTORY),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                OutlinedButton(
                    onClick = onExportCsv,
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = getString(StringResources.EXPORT_CSV))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(getString(StringResources.EXPORT_CSV))
                }


                Button(
                    onClick = onAddProduct,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = getString(StringResources.ADD_PRODUCT))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(getString(StringResources.ADD_PRODUCT))
                }
            }
        }


        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(getString(StringResources.SEARCH_BY_SKU)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = getString(StringResources.SEARCH)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { modifier ->
                            searchFocusRequester?.let { focusRequester ->
                                modifier.focusRequester(focusRequester)
                            } ?: modifier
                        },
                    singleLine = true
                )


                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    item {
                        FilterChip(
                            onClick = { selectedCategory = null },
                            label = { Text("All Types") },
                            selected = selectedCategory == null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }


                    items(JewelryType.entries.toTypedArray()) { category ->
                        FilterChip(
                            onClick = {
                                selectedCategory = if (selectedCategory == category) null else category
                            },
                            label = { Text(category.name) },
                            selected = selectedCategory == category,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }


                    item {
                        FilterChip(
                            onClick = { showLowStockOnly = !showLowStockOnly },
                            label = { Text("Low Stock") },
                            selected = showLowStockOnly,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.error,
                                selectedLabelColor = MaterialTheme.colorScheme.onError
                            )
                        )
                    }
                }
            }
        }


        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                filteredProducts.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = if (products.isEmpty()) {
                                    "No products in inventory—add your first item"
                                } else {
                                    "No products match your filters"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (products.isEmpty()) {
                                Button(
                                    onClick = onAddProduct,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Product")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Your First Product")
                                }
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts) { product ->
                            InventoryProductCard(
                                product = product,
                                onEdit = { onEditProduct(product) },
                                onDelete = { onDeleteProduct(product) },
                                onAdjustStock = { showStockAdjustmentDialog = product }
                            )
                        }
                    }
                }
            }
        }
    }


    showStockAdjustmentDialog?.let { product ->
        StockAdjustmentDialog(
            product = product,
            onDismiss = { showStockAdjustmentDialog = null },
            onConfirm = { delta, reason ->
                onAdjustStock(product, delta, reason)
                showStockAdjustmentDialog = null
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryProductCard(
    product: JewelryProduct,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = product.sku,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = product.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }


                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onAdjustStock) {
                        Icon(Icons.Default.Inventory, contentDescription = "Adjust Stock")
                    }
                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Specifications",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${product.karat}K • ${product.weightGrams}g",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Stock",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${product.quantityInStock} units",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (product.quantityInStock <= 5) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Purchase Price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", product.purchasePrice)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column {
                    Text(
                        text = "Design Fee",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", product.designFee)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockAdjustmentDialog(
    product: JewelryProduct,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: String) -> Unit
) {
    var deltaText by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("MANUAL_CORRECTION") }
    var expanded by remember { mutableStateOf(false) }

    val reasons = listOf(
        "SALE" to "Sale",
        "MANUAL_CORRECTION" to "Manual Correction",
        "DAMAGE" to "Damage",
        "RETURN" to "Return",
        "RESTOCK" to "Restock"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adjust Stock - ${product.sku}") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Current stock: ${product.quantityInStock} units")

                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text("Adjustment (+/-)") },
                    placeholder = { Text("e.g., +5 or -2") },
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = reasons.find { it.first == selectedReason }?.second ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        reasons.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedReason = value
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val delta = deltaText.toIntOrNull() ?: 0
                    if (delta != 0) {
                        onConfirm(delta, selectedReason)
                    }
                },
                enabled = deltaText.toIntOrNull() != null && deltaText.toIntOrNull() != 0
            ) {
                Text("Adjust Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}