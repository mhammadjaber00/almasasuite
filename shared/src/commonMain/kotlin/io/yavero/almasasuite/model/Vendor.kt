package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
data class Vendor(
    val id: String,
    val name: String,
    val contactInfo: String? = null,
    val totalLiabilityBalance: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalIntakeValue: Double = 0.0,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val synced: Boolean = false
) {


    val totalTransacted: Double
        get() = totalIntakeValue


    val hasOutstandingBalance: Boolean
        get() = totalLiabilityBalance > 0.0


    val paymentPercentage: Double
        get() = if (totalIntakeValue > 0.0) {
            (totalPaid / totalIntakeValue) * 100.0
        } else {
            0.0
        }
}


@Serializable
data class VendorRequest(
    val name: String,
    val contactInfo: String? = null,
    val notes: String? = null
)


@Serializable
data class VendorResponse(
    val id: String,
    val name: String,
    val contactInfo: String?,
    val totalLiabilityBalance: Double,
    val totalPaid: Double,
    val totalIntakeValue: Double,
    val isActive: Boolean,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long
)


@Serializable
data class VendorLiabilitySummary(
    val id: String,
    val name: String,
    val totalLiabilityBalance: Double,
    val totalPaid: Double,
    val totalIntakeValue: Double,
    val paymentPercentage: Double,
    val lastIntakeDate: Long?,
    val lastPaymentDate: Long?
)


fun Vendor.toResponse(): VendorResponse {
    return VendorResponse(
        id = id,
        name = name,
        contactInfo = contactInfo,
        totalLiabilityBalance = totalLiabilityBalance,
        totalPaid = totalPaid,
        totalIntakeValue = totalIntakeValue,
        isActive = isActive,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}


fun Vendor.toLiabilitySummary(
    lastIntakeDate: Long? = null,
    lastPaymentDate: Long? = null
): VendorLiabilitySummary {
    return VendorLiabilitySummary(
        id = id,
        name = name,
        totalLiabilityBalance = totalLiabilityBalance,
        totalPaid = totalPaid,
        totalIntakeValue = totalIntakeValue,
        paymentPercentage = paymentPercentage,
        lastIntakeDate = lastIntakeDate,
        lastPaymentDate = lastPaymentDate
    )
}