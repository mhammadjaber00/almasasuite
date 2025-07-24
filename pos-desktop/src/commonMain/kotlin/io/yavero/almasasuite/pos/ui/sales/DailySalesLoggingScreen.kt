package io.yavero.almasasuite.pos.ui.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import io.yavero.almasasuite.model.AuthenticatedUser
import io.yavero.almasasuite.pos.localization.*
import io.yavero.almasasuite.pos.service.JewelryProductService
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySalesLoggingScreen(
    user: AuthenticatedUser,
    onSubmitSales: (List<SalesLogEntry>) -> Unit,
    modifier: Modifier = Modifier
) {
    val jewelryProductService = remember { JewelryProductService() }
    val coroutineScope = rememberCoroutineScope()
    var scannedSku by remember { mutableStateOf("") }
    var salePrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var salesLog by remember { mutableStateOf<List<SalesLogEntry>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSubmitDialog by remember { mutableStateOf(false) }


    val totalItems = salesLog.sumOf { it.quantity }
    val totalValue = salesLog.sumOf { it.quantity * it.unitPrice }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Daily Sales Logging",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Staff: ${user.name} • ${getCurrentDateString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.weight(0.4f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = getString(StringResources.SCAN_OR_ENTER_SKU),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = scannedSku,
                            onValueChange = {
                                scannedSku = it.uppercase()
                                errorMessage = null
                            },
                            label = { Text(getString(StringResources.SKU)) },
                            placeholder = { Text("ALMR18055001") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                isScanning = !isScanning
                                if (isScanning) {

                                    startBarcodeScanning { sku ->
                                        scannedSku = sku
                                        isScanning = false
                                        errorMessage = null
                                    }
                                } else {

                                    stopBarcodeScanning()
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isScanning)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(
                                if (isScanning) Icons.Default.Stop else Icons.Default.QrCodeScanner,
                                contentDescription = if (isScanning) getString(StringResources.STOP_SCANNING) else getString(StringResources.SCAN_BARCODE)
                            )
                        }
                    }


                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.isNotEmpty()) {
                                    quantity = it
                                }
                            },
                            label = { Text(getString(StringResources.QTY)) },
                            modifier = Modifier.weight(0.3f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        OutlinedTextField(
                            value = salePrice,
                            onValueChange = { salePrice = it },
                            label = { Text(getString(StringResources.SALE_PRICE)) },
                            placeholder = { Text(getString(StringResources.PRICE_PLACEHOLDER)) },
                            modifier = Modifier.weight(0.7f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = salePrice.isNotEmpty() && salePrice.toDoubleOrNull() == null
                        )
                    }


                    val pleaseEnterSkuMessage = getString(StringResources.PLEASE_ENTER_SKU)
                    val pleaseEnterValidPriceMessage = getString(StringResources.PLEASE_ENTER_VALID_PRICE)
                    val productForPrefix = getString(StringResources.PRODUCT_FOR)


                    Button(
                        onClick = {
                            val price = salePrice.toDoubleOrNull()
                            val qty = quantity.toIntOrNull() ?: 1

                            when {
                                scannedSku.isBlank() -> errorMessage = pleaseEnterSkuMessage
                                price == null || price <= 0 -> errorMessage = pleaseEnterValidPriceMessage
                                else -> {

                                    coroutineScope.launch {
                                        try {

                                            jewelryProductService.loadProducts()


                                            val product = jewelryProductService.findProductBySku(scannedSku)

                                            if (product == null) {
                                                errorMessage = "Product with SKU '$scannedSku' not found"
                                                return@launch
                                            }

                                            if (product.quantityInStock < qty) {
                                                errorMessage = "Insufficient stock for '$scannedSku'. Available: ${product.quantityInStock}, Requested: $qty"
                                                return@launch
                                            }


                                            val entry = SalesLogEntry(
                                                sku = scannedSku,
                                                productName = product.displayName,
                                                quantity = qty,
                                                unitPrice = price,
                                                timestamp = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                            )

                                            salesLog = salesLog + entry


                                            scannedSku = ""
                                            salePrice = ""
                                            quantity = "1"
                                            errorMessage = null
                                        } catch (e: Exception) {
                                            errorMessage = "Error validating product: ${e.message}"
                                        }
                                    }
                                }
                            }
                        },
                        enabled = scannedSku.isNotBlank() && salePrice.toDoubleOrNull() != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = getString(StringResources.ADD))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(getString(StringResources.ADD_TO_SALES_LOG))
                    }


                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }


            Card(
                modifier = Modifier.weight(0.6f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Sales Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "$totalItems items",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$${String.format("%.2f", totalValue)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }


                    if (salesLog.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items scanned yet\nScan or enter SKUs to begin",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(salesLog.reversed()) { entry ->
                                SalesLogEntryCard(
                                    entry = entry,
                                    onEdit = { editedEntry ->
                                        salesLog = salesLog.map {
                                            if (it.timestamp == entry.timestamp) editedEntry else it
                                        }
                                    },
                                    onRemove = {
                                        salesLog = salesLog.filter { it.timestamp != entry.timestamp }
                                    }
                                )
                            }
                        }
                    }


                    Button(
                        onClick = { showSubmitDialog = true },
                        enabled = salesLog.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Submit")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Submit Day's Sales (${salesLog.size} items)")
                    }
                }
            }
        }
    }


    if (showSubmitDialog) {
        SubmitSalesDialog(
            salesLog = salesLog,
            totalValue = totalValue,
            onConfirm = {
                onSubmitSales(salesLog)
                salesLog = emptyList()
                showSubmitDialog = false
            },
            onDismiss = { showSubmitDialog = false }
        )
    }
}


@Composable
private fun SalesLogEntryCard(
    entry: SalesLogEntry,
    onEdit: (SalesLogEntry) -> Unit,
    onRemove: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.sku,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${entry.quantity}x @ $${String.format("%.2f", entry.unitPrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "$${String.format("%.2f", entry.quantity * entry.unitPrice)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }


    if (showEditDialog) {
        var editQuantity by remember { mutableStateOf(entry.quantity.toString()) }
        var editPrice by remember { mutableStateOf(entry.unitPrice.toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Sales Entry") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Product: ${entry.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "SKU: ${entry.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editQuantity,
                        onValueChange = { if (it.all { char -> char.isDigit() }) editQuantity = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPrice,
                        onValueChange = { editPrice = it },
                        label = { Text("Unit Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )


                    val qty = editQuantity.toIntOrNull() ?: 0
                    val price = editPrice.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "Total: $${String.format("%.2f", qty * price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQuantity = editQuantity.toIntOrNull()
                        val newPrice = editPrice.toDoubleOrNull()

                        if (newQuantity != null && newQuantity > 0 && newPrice != null && newPrice > 0) {
                            val editedEntry = entry.copy(
                                quantity = newQuantity,
                                unitPrice = newPrice
                            )
                            onEdit(editedEntry)
                            showEditDialog = false
                        }
                    },
                    enabled = editQuantity.toIntOrNull()?.let { it > 0 } == true &&
                             editPrice.toDoubleOrNull()?.let { it > 0 } == true
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
private fun SubmitSalesDialog(
    salesLog: List<SalesLogEntry>,
    totalValue: Double,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Day's Sales") },
        text = {
            Column {
                Text("Are you sure you want to submit today's sales?")
                Spacer(modifier = Modifier.height(8.dp))
                Text("• ${salesLog.size} items")
                Text("• Total value: $${String.format("%.2f", totalValue)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This will automatically decrement stock and lock the sales log.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Submit Sales")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


data class SalesLogEntry(
    val sku: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val timestamp: LocalDateTime
)


private fun getCurrentDateString(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${now.dayOfMonth}/${now.monthNumber}/${now.year}"
}


private fun startBarcodeScanning(onScanned: (String) -> Unit) {


    simulateBarcodeInput(onScanned)
}


private fun stopBarcodeScanning() {


    println("[BARCODE] Scanning stopped")
}


private fun simulateBarcodeInput(onScanned: (String) -> Unit) {

    val sampleSkus = listOf(
        "ALMR18055001",
        "ALMB14032002",
        "ALMN22078003",
        "ALME18025004",
        "ALMP21045005"
    )


    kotlinx.coroutines.GlobalScope.launch {
        kotlinx.coroutines.delay(500)
        val randomSku = sampleSkus.random()
        onScanned(randomSku)
        println("[BARCODE] Simulated scan: $randomSku")
    }
}