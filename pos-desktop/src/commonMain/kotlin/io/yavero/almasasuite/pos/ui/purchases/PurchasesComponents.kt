package io.yavero.almasasuite.pos.ui.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.yavero.almasasuite.model.*
import io.yavero.almasasuite.pos.localization.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldIntakeCard(
    intake: GoldIntake,
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
                        text = intake.partyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = when (intake.partyType) {
                            PartyType.SELLER -> MaterialTheme.colorScheme.primaryContainer
                            PartyType.CUSTOMER -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = intake.partyType.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (intake.partyType) {
                                PartyType.SELLER -> MaterialTheme.colorScheme.onPrimaryContainer
                                PartyType.CUSTOMER -> MaterialTheme.colorScheme.onSecondaryContainer
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = formatTimestamp(intake.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gold Details",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${intake.karat}K • ${intake.grams}g",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Design Fee",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", intake.totalDesignFeePaid)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }


            if (intake.partyType == PartyType.SELLER && intake.totalMetalValueOwed > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Metal Value Owed:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", intake.totalMetalValueOwed)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }


            if (!intake.notes.isNullOrBlank()) {
                Text(
                    text = "Notes: ${intake.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorCard(
    vendor: Vendor,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
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
                Text(
                    text = vendor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (vendor.hasOutstandingBalance) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Outstanding",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Intake",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", vendor.totalIntakeValue)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Outstanding",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", vendor.totalLiabilityBalance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (vendor.totalLiabilityBalance > 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }


            if (vendor.totalIntakeValue > 0) {
                LinearProgressIndicator(
                    progress = { (vendor.paymentPercentage / 100.0).toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${String.format("%.1f", vendor.paymentPercentage)}% paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            vendor.contactInfo?.let { contactInfo ->
                if (contactInfo.isNotBlank()) {
                    Text(
                        text = contactInfo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPaymentCard(
    payment: VendorPayment,
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
                Text(
                    text = "$${String.format("%.2f", payment.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = formatTimestamp(payment.paidAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = payment.paymentMethodDisplay,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (payment.hasReference) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Reference",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = payment.paymentReference!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }


            if (!payment.notes.isNullOrBlank()) {
                Text(
                    text = "Notes: ${payment.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorLiabilityCard(
    vendor: Vendor,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        )
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
                Text(
                    text = vendor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$${String.format("%.2f", vendor.totalLiabilityBalance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Intake Value",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", vendor.totalIntakeValue)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Paid So Far",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format("%.2f", vendor.totalPaid)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }


            LinearProgressIndicator(
                progress = { (vendor.paymentPercentage / 100.0).toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            )

            Text(
                text = "${String.format("%.1f", vendor.paymentPercentage)}% paid • ${String.format("%.1f", 100 - vendor.paymentPercentage)}% outstanding",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldIntakeFormDialog(
    onDismiss: () -> Unit,
    onSubmit: (GoldIntakeRequest) -> Unit
) {
    var partyType by remember { mutableStateOf(PartyType.SELLER) }
    var partyName by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf("") }
    var grams by remember { mutableStateOf("") }
    var designFeePerGram by remember { mutableStateOf("") }
    var metalValuePerGram by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var showValidationErrors by remember { mutableStateOf(false) }


    val isValid = partyName.isNotBlank() &&
            karat.toIntOrNull()?.let { it > 0 } == true &&
            grams.toDoubleOrNull()?.let { it > 0 } == true &&
            designFeePerGram.toDoubleOrNull()?.let { it >= 0 } == true &&
            (partyType == PartyType.CUSTOMER || metalValuePerGram.toDoubleOrNull()?.let { it >= 0 } == true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Gold Intake") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(500.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {

                Text(
                        text = "Party Type",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { partyType = PartyType.SELLER },
                            label = { Text("Seller") },
                            selected = partyType == PartyType.SELLER,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        FilterChip(
                            onClick = { partyType = PartyType.CUSTOMER },
                            label = { Text("Customer") },
                            selected = partyType == PartyType.CUSTOMER,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                            )
                        )
                    }
                }

                item {

                OutlinedTextField(
                        value = partyName,
                        onValueChange = { partyName = it },
                        label = { Text("${partyType.name.lowercase().replaceFirstChar { it.uppercase() }} Name") },
                        placeholder = { Text(getString(StringResources.ENTER_NAME)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && partyName.isBlank()
                    )
                    if (showValidationErrors && partyName.isBlank()) {
                        Text(
                            text = "Name is required",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = karat,
                            onValueChange = { if (it.all { char -> char.isDigit() }) karat = it },
                            label = { Text(getString(StringResources.KARAT)) },
                            placeholder = { Text("18") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showValidationErrors && (karat.toIntOrNull()?.let { it <= 0 } != false)
                        )

                        OutlinedTextField(
                            value = grams,
                            onValueChange = { grams = it },
                            label = { Text("Grams") },
                            placeholder = { Text("5.5") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = showValidationErrors && (grams.toDoubleOrNull()?.let { it <= 0 } != false)
                        )
                    }
                    if (showValidationErrors) {
                        if (karat.toIntOrNull()?.let { it <= 0 } != false) {
                            Text(
                                text = "Karat must be greater than 0",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (grams.toDoubleOrNull()?.let { it <= 0 } != false) {
                            Text(
                                text = "Weight must be greater than 0",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item {

                OutlinedTextField(
                        value = designFeePerGram,
                        onValueChange = { designFeePerGram = it },
                        label = { Text("Design Fee per Gram") },
                        placeholder = { Text("10.00") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = showValidationErrors && (designFeePerGram.toDoubleOrNull()?.let { it < 0 } != false)
                    )
                    if (showValidationErrors && designFeePerGram.toDoubleOrNull()?.let { it < 0 } != false) {
                        Text(
                            text = "Design fee cannot be negative",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (partyType == PartyType.SELLER) {
                    item {

                    OutlinedTextField(
                            value = metalValuePerGram,
                            onValueChange = { metalValuePerGram = it },
                            label = { Text("Metal Value per Gram (Owed)") },
                            placeholder = { Text("50.00") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = showValidationErrors && (metalValuePerGram.toDoubleOrNull()?.let { it < 0 } != false)
                        )
                        Text(
                            text = "Amount we owe the seller for the metal value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (showValidationErrors && metalValuePerGram.toDoubleOrNull()?.let { it < 0 } != false) {
                            Text(
                                text = "Metal value cannot be negative",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item {

                OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("Additional information...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }


                item {
                    val karatValue = karat.toIntOrNull() ?: 0
                    val gramsValue = grams.toDoubleOrNull() ?: 0.0
                    val designFeeValue = designFeePerGram.toDoubleOrNull() ?: 0.0
                    val metalValueValue = if (partyType == PartyType.SELLER) metalValuePerGram.toDoubleOrNull() ?: 0.0 else 0.0

                    val totalDesignFee = designFeeValue * gramsValue
                    val totalMetalValue = metalValueValue * gramsValue

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Total Design Fee: $${String.format("%.2f", totalDesignFee)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (partyType == PartyType.SELLER && totalMetalValue > 0) {
                                Text(
                                    text = "Total Metal Value Owed: $${String.format("%.2f", totalMetalValue)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        val request = GoldIntakeRequest(
                            vendorId = null,
                            partyType = partyType,
                            partyName = partyName,
                            karat = karat.toInt(),
                            grams = grams.toDouble(),
                            designFeePerGram = designFeePerGram.toDouble(),
                            metalValuePerGram = if (partyType == PartyType.SELLER) metalValuePerGram.toDoubleOrNull() ?: 0.0 else 0.0,
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                        onSubmit(request)
                    } else {
                        showValidationErrors = true
                    }
                },
                enabled = isValid
            ) {
                Text("Record Intake")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorPaymentFormDialog(
    vendors: List<Vendor>,
    selectedVendor: Vendor?,
    onDismiss: () -> Unit,
    onSubmit: (VendorPaymentRequest) -> Unit
) {
    var selectedVendorId by remember { mutableStateOf(selectedVendor?.id ?: "") }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(VendorPaymentMethod.CASH) }
    var paymentReference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var showValidationErrors by remember { mutableStateOf(false) }


    val vendor = vendors.find { it.id == selectedVendorId }


    val isValid = selectedVendorId.isNotBlank() &&
            amount.toDoubleOrNull()?.let { it > 0 } == true &&
            (vendor?.totalLiabilityBalance ?: 0.0) >= (amount.toDoubleOrNull() ?: 0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Vendor Payment") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = vendor?.name ?: "Select vendor...",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Vendor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        isError = showValidationErrors && selectedVendorId.isBlank()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vendors.filter { it.totalLiabilityBalance > 0 }.forEach { vendor ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(vendor.name)
                                        Text(
                                            "Outstanding: $${String.format("%.2f", vendor.totalLiabilityBalance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    selectedVendorId = vendor.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }


                if (vendor != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Vendor Information",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Outstanding Balance: $${String.format("%.2f", vendor.totalLiabilityBalance)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Total Intake Value: $${String.format("%.2f", vendor.totalIntakeValue)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }


                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Payment Amount") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showValidationErrors && (amount.toDoubleOrNull()?.let { it <= 0 } != false ||
                            (vendor?.totalLiabilityBalance ?: 0.0) < (amount.toDoubleOrNull() ?: 0.0))
                )
                if (showValidationErrors) {
                    if (amount.toDoubleOrNull()?.let { it <= 0 } != false) {
                        Text(
                            text = "Amount must be greater than 0",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else if ((vendor?.totalLiabilityBalance ?: 0.0) < (amount.toDoubleOrNull() ?: 0.0)) {
                        Text(
                            text = "Amount cannot exceed outstanding balance",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }


                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VendorPaymentMethod.entries.forEach { method ->
                        FilterChip(
                            onClick = { paymentMethod = method },
                            label = { Text(method.name.replace("_", " ")) },
                            selected = paymentMethod == method,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }


                OutlinedTextField(
                    value = paymentReference,
                    onValueChange = { paymentReference = it },
                    label = { Text("Reference (Optional)") },
                    placeholder = { Text("Check number, transfer ID, etc.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )


                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Additional information...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        val request = VendorPaymentRequest(
                            vendorId = selectedVendorId,
                            amount = amount.toDouble(),
                            paymentMethod = paymentMethod,
                            paymentReference = paymentReference.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() },
                            paidAt = Clock.System.now().toEpochMilliseconds()
                        )
                        onSubmit(request)
                    } else {
                        showValidationErrors = true
                    }
                },
                enabled = isValid
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


private fun formatTimestamp(timestamp: Long): String {
    val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth}/${localDateTime.monthNumber}/${localDateTime.year}"
}