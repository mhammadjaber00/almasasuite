package io.yavero.almasasuite.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table


object Vendors : Table("vendors") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val contactInfo = text("contact_info").nullable()
    val totalLiabilityBalance = decimal("total_liability_balance", 12, 2)
    val totalPaid = decimal("total_paid", 12, 2)
    val totalIntakeValue = decimal("total_intake_value", 12, 2)
    val isActive = bool("is_active")
    val notes = text("notes").nullable()
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    val createdBy = varchar("created_by", 36)
    val synced = bool("synced")

    override val primaryKey = PrimaryKey(id)
}


data class Vendor(
    val id: String,
    val name: String,
    val contactInfo: String?,
    val totalLiabilityBalance: Double,
    val totalPaid: Double,
    val totalIntakeValue: Double,
    val isActive: Boolean,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val synced: Boolean
)


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
    val updatedAt: Long,
    val paymentPercentage: Double,
    val hasOutstandingBalance: Boolean
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
    val lastPaymentDate: Long?,
    val intakeCount: Int,
    val paymentCount: Int
)


fun Vendor.toResponse(): VendorResponse {
    val paymentPercentage = if (totalIntakeValue > 0.0) {
        (totalPaid / totalIntakeValue) * 100.0
    } else {
        0.0
    }

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
        updatedAt = updatedAt,
        paymentPercentage = paymentPercentage,
        hasOutstandingBalance = totalLiabilityBalance > 0.0
    )
}