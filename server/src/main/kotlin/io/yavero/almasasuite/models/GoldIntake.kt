package io.yavero.almasasuite.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import io.yavero.almasasuite.model.PartyType


object GoldIntakes : Table("gold_intakes") {
    val id = varchar("id", 36)
    val vendorId = varchar("vendor_id", 36).nullable()
    val partyType = varchar("party_type", 20)
    val partyName = varchar("party_name", 255)
    val karat = integer("karat")
    val grams = decimal("grams", 10, 3)
    val designFeePerGram = decimal("design_fee_per_gram", 10, 2)
    val metalValuePerGram = decimal("metal_value_per_gram", 10, 2)
    val totalDesignFeePaid = decimal("total_design_fee_paid", 12, 2)
    val totalMetalValueOwed = decimal("total_metal_value_owed", 12, 2)
    val notes = text("notes").nullable()
    val createdAt = long("created_at")
    val createdBy = varchar("created_by", 36)
    val synced = bool("synced")

    override val primaryKey = PrimaryKey(id)
}


data class GoldIntake(
    val id: String,
    val vendorId: String?,
    val partyType: PartyType,
    val partyName: String,
    val karat: Int,
    val grams: Double,
    val designFeePerGram: Double,
    val metalValuePerGram: Double,
    val totalDesignFeePaid: Double,
    val totalMetalValueOwed: Double,
    val notes: String?,
    val createdAt: Long,
    val createdBy: String,
    val synced: Boolean
)


@Serializable
data class GoldIntakeRequest(
    val vendorId: String? = null,
    val partyType: PartyType,
    val partyName: String,
    val karat: Int,
    val grams: Double,
    val designFeePerGram: Double,
    val metalValuePerGram: Double = 0.0,
    val notes: String? = null
)


@Serializable
data class GoldIntakeResponse(
    val id: String,
    val vendorId: String?,
    val vendorName: String?,
    val partyType: PartyType,
    val partyName: String,
    val karat: Int,
    val grams: Double,
    val designFeePerGram: Double,
    val metalValuePerGram: Double,
    val totalDesignFeePaid: Double,
    val totalMetalValueOwed: Double,
    val notes: String?,
    val createdAt: Long,
    val createdBy: String,
    val createdByName: String?
)


fun GoldIntake.toResponse(
    vendorName: String? = null,
    createdByName: String? = null
): GoldIntakeResponse {
    return GoldIntakeResponse(
        id = id,
        vendorId = vendorId,
        vendorName = vendorName,
        partyType = partyType,
        partyName = partyName,
        karat = karat,
        grams = grams,
        designFeePerGram = designFeePerGram,
        metalValuePerGram = metalValuePerGram,
        totalDesignFeePaid = totalDesignFeePaid,
        totalMetalValueOwed = totalMetalValueOwed,
        notes = notes,
        createdAt = createdAt,
        createdBy = createdBy,
        createdByName = createdByName
    )
}