package io.yavero.almasasuite.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import io.yavero.almasasuite.model.VendorPaymentMethod


object VendorPayments : Table("vendor_payments") {
    val id = varchar("id", 36)
    val vendorId = varchar("vendor_id", 36)
    val amount = decimal("amount", 12, 2)
    val paymentMethod = varchar("payment_method", 20)
    val paymentReference = varchar("payment_reference", 255).nullable()
    val notes = text("notes").nullable()
    val paidAt = long("paid_at")
    val recordedAt = long("recorded_at")
    val recordedBy = varchar("recorded_by", 36)
    val synced = bool("synced")

    override val primaryKey = PrimaryKey(id)
}


data class VendorPayment(
    val id: String,
    val vendorId: String,
    val amount: Double,
    val paymentMethod: VendorPaymentMethod,
    val paymentReference: String?,
    val notes: String?,
    val paidAt: Long,
    val recordedAt: Long,
    val recordedBy: String,
    val synced: Boolean
)


@Serializable
data class VendorPaymentRequest(
    val vendorId: String,
    val amount: Double,
    val paymentMethod: VendorPaymentMethod,
    val paymentReference: String? = null,
    val notes: String? = null,
    val paidAt: Long? = null
)


@Serializable
data class VendorPaymentResponse(
    val id: String,
    val vendorId: String,
    val vendorName: String,
    val amount: Double,
    val paymentMethod: VendorPaymentMethod,
    val paymentMethodDisplay: String,
    val paymentReference: String?,
    val notes: String?,
    val paidAt: Long,
    val recordedAt: Long,
    val recordedBy: String,
    val recordedByName: String?,
    val displayString: String
)


@Serializable
data class VendorPaymentSummary(
    val vendorId: String,
    val vendorName: String,
    val totalPaid: Double,
    val paymentCount: Int,
    val lastPaymentDate: Long?,
    val lastPaymentAmount: Double?
)


fun VendorPayment.toResponse(
    vendorName: String,
    recordedByName: String? = null
): VendorPaymentResponse {
    val paymentMethodDisplay = when (paymentMethod) {
        VendorPaymentMethod.CASH -> "Cash"
        VendorPaymentMethod.CHECK -> "Check"
        VendorPaymentMethod.BANK_TRANSFER -> "Bank Transfer"
        VendorPaymentMethod.OTHER -> "Other"
    }

    val displayString = buildString {
        append("$${String.format("%.2f", amount)} via $paymentMethodDisplay")
        if (!paymentReference.isNullOrBlank()) {
            append(" (Ref: $paymentReference)")
        }
    }

    return VendorPaymentResponse(
        id = id,
        vendorId = vendorId,
        vendorName = vendorName,
        amount = amount,
        paymentMethod = paymentMethod,
        paymentMethodDisplay = paymentMethodDisplay,
        paymentReference = paymentReference,
        notes = notes,
        paidAt = paidAt,
        recordedAt = recordedAt,
        recordedBy = recordedBy,
        recordedByName = recordedByName,
        displayString = displayString
    )
}