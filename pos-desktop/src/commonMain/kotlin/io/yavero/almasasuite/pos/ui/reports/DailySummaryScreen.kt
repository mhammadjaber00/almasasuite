package io.yavero.almasasuite.pos.ui.reports

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.model.AuthenticatedUser
import io.yavero.almasasuite.pos.data.local.LocalSaleWithItems
import io.yavero.almasasuite.pos.localization.*
import kotlinx.datetime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryScreen(
    user: AuthenticatedUser,
    onExportCsv: (DailySummaryReport) -> Unit,
    onPrintReport: (DailySummaryReport) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStartDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var selectedEndDate by remember { mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerType by remember { mutableStateOf(DatePickerType.START) }
    var isLoading by remember { mutableStateOf(false) }
    var summaryReport by remember { mutableStateOf<DailySummaryReport?>(null) }


    LaunchedEffect(selectedStartDate, selectedEndDate) {
        isLoading = true


        summaryReport = generateSampleReport(selectedStartDate, selectedEndDate)
        isLoading = false
    }

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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Daily Summary Report",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Date Range:",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedButton(
                        onClick = {
                            datePickerType = DatePickerType.START
                            showDatePicker = true
                        }
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Start Date")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatDate(selectedStartDate))
                    }

                    Text(getString(StringResources.TO))

                    OutlinedButton(
                        onClick = {
                            datePickerType = DatePickerType.END
                            showDatePicker = true
                        }
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "End Date")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(formatDate(selectedEndDate))
                    }

                    Spacer(modifier = Modifier.weight(1f))


                    OutlinedButton(
                        onClick = {
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            selectedStartDate = today
                            selectedEndDate = today
                        }
                    ) {
                        Text(getString(StringResources.TODAY))
                    }

                    OutlinedButton(
                        onClick = {
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            selectedStartDate = today.minus(6, DateTimeUnit.DAY)
                            selectedEndDate = today
                        }
                    ) {
                        Text("Last 7 Days")
                    }
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (summaryReport != null) {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                item {
                    SummaryMetricsCards(summaryReport!!)
                }


                item {
                    PaymentMethodBreakdown(summaryReport!!.paymentBreakdown)
                }


                item {
                    TopSellingItems(summaryReport!!.topSellingItems)
                }


                item {
                    StockStatusCard(summaryReport!!.stockStatus)
                }


                item {
                    ExportActionsCard(
                        onExportCsv = { onExportCsv(summaryReport!!) },
                        onPrintReport = { onPrintReport(summaryReport!!) }
                    )
                }
            }
        } else {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Assessment,
                        contentDescription = "No Data",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No sales data found for the selected date range",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }


    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = if (datePickerType == DatePickerType.START) selectedStartDate else selectedEndDate,
            onDateSelected = { date ->
                if (datePickerType == DatePickerType.START) {
                    selectedStartDate = date
                } else {
                    selectedEndDate = date
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            title = if (datePickerType == DatePickerType.START) "Select Start Date" else "Select End Date"
        )
    }
}


@Composable
private fun SummaryMetricsCards(report: DailySummaryReport) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        MetricCard(
            title = "Gross Sales",
            value = "$${String.format("%.2f", report.grossSales)}",
            subtitle = "${report.totalItems} items sold",
            icon = Icons.Default.TrendingUp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )


        MetricCard(
            title = "Net Sales",
            value = "$${String.format("%.2f", report.netSales)}",
            subtitle = "After adjustments",
            icon = Icons.Default.AccountBalance,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )


        MetricCard(
            title = "Profit",
            value = "$${String.format("%.2f", report.profit)}",
            subtitle = "${String.format("%.1f", report.profitMargin)}% margin",
            icon = Icons.Default.Savings,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )


        MetricCard(
            title = "Cost of Goods",
            value = "$${String.format("%.2f", report.costOfGoods)}",
            subtitle = "Purchase + Design",
            icon = Icons.Default.Receipt,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun PaymentMethodBreakdown(paymentBreakdown: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Payment Method Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            paymentBreakdown.forEach { (method, amount) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            when (method.uppercase()) {
                                "CASH" -> Icons.Default.Money
                                "CARD" -> Icons.Default.CreditCard
                                else -> Icons.Default.Payment
                            },
                            contentDescription = method,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = method,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "$${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
private fun TopSellingItems(topItems: List<TopSellingItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Top Selling Items",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            topItems.take(5).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = item.sku,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${item.type} • ${item.karat}K • ${item.weightGrams}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${item.quantitySold} sold",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$${String.format("%.2f", item.totalRevenue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun StockStatusCard(stockStatus: StockStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Remaining Stock",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stockStatus.totalItems}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Low Stock Items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stockStatus.lowStockItems}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stockStatus.lowStockItems > 0)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Total Value",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", stockStatus.totalValue)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


@Composable
private fun ExportActionsCard(
    onExportCsv: () -> Unit,
    onPrintReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Export Options",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = getString(StringResources.EXPORT_CSV))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(getString(StringResources.EXPORT_CSV))
                }

                Button(
                    onClick = onPrintReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, contentDescription = getString(StringResources.PRINT_REPORT))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(getString(StringResources.PRINT_REPORT))
                }
            }
        }
    }
}


data class DailySummaryReport(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val grossSales: Double,
    val netSales: Double,
    val profit: Double,
    val profitMargin: Double,
    val costOfGoods: Double,
    val totalItems: Int,
    val paymentBreakdown: Map<String, Double>,
    val topSellingItems: List<TopSellingItem>,
    val stockStatus: StockStatus
)

data class TopSellingItem(
    val sku: String,
    val type: String,
    val karat: Int,
    val weightGrams: Double,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class StockStatus(
    val totalItems: Int,
    val lowStockItems: Int,
    val totalValue: Double
)

enum class DatePickerType {
    START, END
}


private fun formatDate(date: LocalDate): String {
    return "${date.dayOfMonth}/${date.monthNumber}/${date.year}"
}


private fun generateSampleReport(startDate: LocalDate, endDate: LocalDate): DailySummaryReport {
    return DailySummaryReport(
        startDate = startDate,
        endDate = endDate,
        grossSales = 2450.00,
        netSales = 2450.00,
        profit = 980.00,
        profitMargin = 40.0,
        costOfGoods = 1470.00,
        totalItems = 8,
        paymentBreakdown = mapOf(
            "Cash" to 1200.00,
            "Card" to 1250.00
        ),
        topSellingItems = listOf(
            TopSellingItem("ALMR18055001", "RING", 18, 5.5, 2, 800.00),
            TopSellingItem("ALMB14032002", "BRACELET", 14, 3.2, 1, 450.00),
            TopSellingItem("ALMN22078003", "NECKLACE", 22, 7.8, 1, 650.00)
        ),
        stockStatus = StockStatus(
            totalItems = 156,
            lowStockItems = 3,
            totalValue = 45600.00
        )
    )
}


@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    title: String
) {
    var tempYear by remember { mutableStateOf(selectedDate.year) }
    var tempMonth by remember { mutableStateOf(selectedDate.monthNumber) }
    var tempDay by remember { mutableStateOf(selectedDate.dayOfMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedTextField(
                    value = tempYear.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { year ->
                            if (year in 2020..2030) {
                                tempYear = year
                            }
                        }
                    },
                    label = { Text("Year") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = tempMonth.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { month ->
                            if (month in 1..12) {
                                tempMonth = month
                            }
                        }
                    },
                    label = { Text("Month") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                OutlinedTextField(
                    value = tempDay.toString(),
                    onValueChange = { value ->
                        value.toIntOrNull()?.let { day ->
                            if (day in 1..31) {
                                tempDay = day
                            }
                        }
                    },
                    label = { Text("Day") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )


                Text(
                    text = "Selected: $tempDay/$tempMonth/$tempYear",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val newDate = LocalDate(tempYear, tempMonth, tempDay)
                        onDateSelected(newDate)
                    } catch (e: Exception) {

                        onDismiss()
                    }
                }
            ) {
                Text("Select")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}