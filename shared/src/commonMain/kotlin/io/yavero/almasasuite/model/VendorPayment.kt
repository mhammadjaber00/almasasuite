package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
enum class VendorPaymentMethod {
    CASH, CHECK, BANK_TRANSFER, OTHER;

    companion object {
        fun fromString(value: String): VendorPaymentMethod {
            return when (value.uppercase()) {
                "CASH" -> CASH
                "CHECK" -> CHECK
                "BANK_TRANSFER" -> BANK_TRANSFER
                "OTHER" -> OTHER
                else -> CASH
            }
        }

        fun toDbValue(method: VendorPaymentMethod): String {
            return method.name.lowercase()
        }
    }
}


@Serializable
data class VendorPayment(
    val id: String,
    val vendorId: String,
    val amount: Double,
    val paymentMethod: VendorPaymentMethod,
    val paymentReference: String? = null,
    val notes: String? = null,
    val paidAt: Long,
    val recordedAt: Long,
    val recordedBy: String,
    val synced: Boolean = false
) {


    val hasReference: Boolean
        get() = !paymentReference.isNullOrBlank()


    val paymentMethodDisplay: String
        get() = when (paymentMethod) {
            VendorPaymentMethod.CASH -> "Cash"
            VendorPaymentMethod.CHECK -> "Check"
            VendorPaymentMethod.BANK_TRANSFER -> "Bank Transfer"
            VendorPaymentMethod.OTHER -> "Other"
        }


    val displayString: String
        get() = buildString {
            append("$${String.format("%.2f", amount)} via $paymentMethodDisplay")
            if (hasReference) {
                append(" (Ref: $paymentReference)")
            }
        }
}


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
    val paymentReference: String?,
    val notes: String?,
    val paidAt: Long,
    val recordedAt: Long,
    val recordedBy: String
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


fun VendorPayment.toResponse(vendorName: String): VendorPaymentResponse {
    return VendorPaymentResponse(
        id = id,
        vendorId = vendorId,
        vendorName = vendorName,
        amount = amount,
        paymentMethod = paymentMethod,
        paymentReference = paymentReference,
        notes = notes,
        paidAt = paidAt,
        recordedAt = recordedAt,
        recordedBy = recordedBy
    )
}