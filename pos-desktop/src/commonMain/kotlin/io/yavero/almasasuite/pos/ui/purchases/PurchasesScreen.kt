package io.yavero.almasasuite.pos.ui.purchases

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.model.AuthenticatedUser
import io.yavero.almasasuite.model.GoldIntake
import io.yavero.almasasuite.model.Vendor
import io.yavero.almasasuite.model.VendorPayment
import io.yavero.almasasuite.pos.service.GoldIntakeService
import io.yavero.almasasuite.pos.service.GoldIntakeResult
import io.yavero.almasasuite.pos.service.VendorPaymentResult
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    user: AuthenticatedUser,
    modifier: Modifier = Modifier
) {
    val goldIntakeService = remember { GoldIntakeService() }
    val coroutineScope = rememberCoroutineScope()

    var currentTab by remember { mutableStateOf(PurchasesTab.GOLD_INTAKE) }
    var showGoldIntakeForm by remember { mutableStateOf(false) }
    var showVendorPaymentForm by remember { mutableStateOf(false) }
    var selectedVendor by remember { mutableStateOf<Vendor?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }


    var goldIntakes by remember { mutableStateOf<List<GoldIntake>>(emptyList()) }
    var vendors by remember { mutableStateOf<List<Vendor>>(emptyList()) }
    var vendorPayments by remember { mutableStateOf<List<VendorPayment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }


    fun refreshGoldIntakes() {
        coroutineScope.launch {
            isLoading = true
            try {


            } catch (e: Exception) {
                errorMessage = "Failed to refresh gold intakes: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshVendors() {
        coroutineScope.launch {
            isLoading = true
            try {


            } catch (e: Exception) {
                errorMessage = "Failed to refresh vendors: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshVendorPayments() {
        coroutineScope.launch {
            isLoading = true
            try {


            } catch (e: Exception) {
                errorMessage = "Failed to refresh vendor payments: ${e.message}"
            } finally {
                isLoading = false
            }
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
                text = "Purchases & Gold Intake",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = { showGoldIntakeForm = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Record Intake")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Gold Intake")
                }


                OutlinedButton(
                    onClick = { showVendorPaymentForm = true }
                ) {
                    Icon(Icons.Default.Payment, contentDescription = "Pay Vendor")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay Vendor")
                }
            }
        }


        TabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PurchasesTab.entries.forEach { tab ->
                Tab(
                    selected = currentTab == tab,
                    onClick = { currentTab = tab },
                    text = { Text(tab.displayName) },
                    icon = {
                        Icon(
                            when (tab) {
                                PurchasesTab.GOLD_INTAKE -> Icons.Default.Inventory2
                                PurchasesTab.VENDORS -> Icons.Default.People
                                PurchasesTab.PAYMENTS -> Icons.Default.Payment
                                PurchasesTab.LIABILITY_REPORT -> Icons.Default.Assessment
                            },
                            contentDescription = tab.displayName
                        )
                    }
                )
            }
        }


        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            when (currentTab) {
                PurchasesTab.GOLD_INTAKE -> {
                    GoldIntakeTab(
                        goldIntakes = goldIntakes,
                        isLoading = isLoading,
                        onRefresh = { refreshGoldIntakes() }
                    )
                }
                PurchasesTab.VENDORS -> {
                    VendorsTab(
                        vendors = vendors,
                        isLoading = isLoading,
                        onVendorSelected = { selectedVendor = it },
                        onRefresh = { refreshVendors() }
                    )
                }
                PurchasesTab.PAYMENTS -> {
                    PaymentsTab(
                        payments = vendorPayments,
                        isLoading = isLoading,
                        onRefresh = { refreshVendorPayments() }
                    )
                }
                PurchasesTab.LIABILITY_REPORT -> {
                    LiabilityReportTab(
                        vendors = vendors,
                        isLoading = isLoading,
                        onRefresh = { refreshVendors() }
                    )
                }
            }
        }
    }


    if (showGoldIntakeForm) {
        GoldIntakeFormDialog(
            onDismiss = { showGoldIntakeForm = false },
            onSubmit = { intake ->
                coroutineScope.launch {
                    when (val result = goldIntakeService.submitGoldIntake(intake)) {
                        is GoldIntakeResult.Success -> {
                            successMessage = "Gold intake recorded successfully"
                            errorMessage = null
                            showGoldIntakeForm = false
                            refreshGoldIntakes()
                            refreshVendors()
                        }
                        is GoldIntakeResult.Error -> {
                            errorMessage = result.message
                            successMessage = null
                        }
                    }
                }
            }
        )
    }


    if (showVendorPaymentForm) {
        VendorPaymentFormDialog(
            vendors = vendors,
            selectedVendor = selectedVendor,
            onDismiss = {
                showVendorPaymentForm = false
                selectedVendor = null
            },
            onSubmit = { payment ->
                coroutineScope.launch {
                    when (val result = goldIntakeService.submitVendorPayment(
                        vendorId = payment.vendorId,
                        amount = payment.amount,
                        notes = payment.notes
                    )) {
                        is VendorPaymentResult.Success -> {
                            successMessage = "Vendor payment recorded successfully"
                            errorMessage = null
                            showVendorPaymentForm = false
                            selectedVendor = null
                            refreshVendorPayments()
                            refreshVendors()
                        }
                        is VendorPaymentResult.Error -> {
                            errorMessage = result.message
                            successMessage = null
                        }
                    }
                }
            }
        )
    }


    if (successMessage != null) {
        LaunchedEffect(successMessage) {
            kotlinx.coroutines.delay(3000)
            successMessage = null
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = successMessage!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }


    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }
}


@Composable
private fun GoldIntakeTab(
    goldIntakes: List<GoldIntake>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Gold Intake History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            goldIntakes.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Inventory2,
                            contentDescription = "No Intakes",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No gold intakes recorded yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Click 'Record Gold Intake' to add your first entry",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(goldIntakes) { intake ->
                        GoldIntakeCard(intake = intake)
                    }
                }
            }
        }
    }
}


@Composable
private fun VendorsTab(
    vendors: List<Vendor>,
    isLoading: Boolean,
    onVendorSelected: (Vendor) -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vendors & Sellers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            vendors.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "No Vendors",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No vendors found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Vendors are created automatically when recording gold intake from sellers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vendors) { vendor ->
                        VendorCard(
                            vendor = vendor,
                            onClick = { onVendorSelected(vendor) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PaymentsTab(
    payments: List<VendorPayment>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payment History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            payments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Payment,
                            contentDescription = "No Payments",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No payments recorded yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Click 'Pay Vendor' to record your first payment",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(payments) { payment ->
                        VendorPaymentCard(payment = payment)
                    }
                }
            }
        }
    }
}


@Composable
private fun LiabilityReportTab(
    vendors: List<Vendor>,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Liability Report",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {

                        exportLiabilityReportToCsv(vendors)
                    }
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "Export")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export CSV")
                }

                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }


        val totalLiability = vendors.sumOf { it.totalLiabilityBalance }
        val vendorsWithLiability = vendors.count { it.totalLiabilityBalance > 0 }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Total Outstanding",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "$${String.format("%.2f", totalLiability)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Vendors with Liability",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = vendorsWithLiability.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }


        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            vendorsWithLiability == 0 -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "No Liabilities",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "No outstanding liabilities",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "All vendor payments are up to date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vendors.filter { it.totalLiabilityBalance > 0 }) { vendor ->
                        VendorLiabilityCard(vendor = vendor)
                    }
                }
            }
        }
    }
}


enum class PurchasesTab(val displayName: String) {
    GOLD_INTAKE("Gold Intake"),
    VENDORS("Vendors"),
    PAYMENTS("Payments"),
    LIABILITY_REPORT("Liability Report")
}


private fun exportLiabilityReportToCsv(vendors: List<Vendor>) {
    try {
        val csvContent = generateLiabilityReportCsv(vendors)
        val filename = "liability_report_${System.currentTimeMillis()}.csv"



        println("CSV Export: $filename")
        println(csvContent)


    } catch (e: Exception) {
        println("Error exporting CSV: ${e.message}")
    }
}


private fun generateLiabilityReportCsv(vendors: List<Vendor>): String {
    val header = "Vendor Name,Contact Info,Total Liability Balance,Total Paid,Total Intake Value,Created At"
    val rows = vendors.map { vendor ->
        "${vendor.name}," +
        "\"${vendor.contactInfo ?: ""}\","+
        "${vendor.totalLiabilityBalance}," +
        "${vendor.totalPaid}," +
        "${vendor.totalIntakeValue}," +
        "${formatTimestamp(vendor.createdAt)}"
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