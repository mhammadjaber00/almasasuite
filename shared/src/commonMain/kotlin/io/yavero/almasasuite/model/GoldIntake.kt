package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
enum class PartyType {
    SELLER, CUSTOMER;

    companion object {
        fun fromString(value: String): PartyType {
            return when (value.uppercase()) {
                "SELLER" -> SELLER
                "CUSTOMER" -> CUSTOMER
                else -> CUSTOMER
            }
        }

        fun toDbValue(type: PartyType): String {
            return type.name.lowercase()
        }
    }
}


@Serializable
data class GoldIntake(
    val id: String,
    val vendorId: String? = null,
    val partyType: PartyType,
    val partyName: String,
    val karat: Int,
    val grams: Double,
    val designFeePerGram: Double,
    val metalValuePerGram: Double = 0.0,
    val totalDesignFeePaid: Double,
    val totalMetalValueOwed: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long,
    val createdBy: String,
    val synced: Boolean = false
) {


    fun calculateTotalDesignFee(): Double = designFeePerGram * grams


    fun calculateTotalMetalValue(): Double = metalValuePerGram * grams


    val createsLiability: Boolean
        get() = partyType == PartyType.SELLER && metalValuePerGram > 0.0
}


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
    val createdBy: String
)


fun GoldIntake.toResponse(): GoldIntakeResponse {
    return GoldIntakeResponse(
        id = id,
        vendorId = vendorId,
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
        createdBy = createdBy
    )
}