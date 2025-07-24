package io.yavero.almasasuite.services

import io.yavero.almasasuite.models.*
import io.yavero.almasasuite.plugins.dbQuery
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*


class GoldIntakeService {


    fun validateGoldIntakeRequest(request: GoldIntakeRequest): String? {
        return when {
            request.karat <= 0 -> "Karat must be positive"
            request.grams <= 0 -> "Weight in grams must be positive"
            request.designFeePerGram < 0 -> "Design fee per gram cannot be negative"
            request.metalValuePerGram < 0 -> "Metal value per gram cannot be negative"
            request.partyName.isBlank() -> "Party name is required"
            else -> null
        }
    }


    fun validateVendorPaymentRequest(request: VendorPaymentRequest): String? {
        return when {
            request.amount <= 0 -> "Payment amount must be positive"
            request.vendorId.isBlank() -> "Vendor ID is required"
            else -> null
        }
    }


    private fun mapRowToGoldIntake(row: ResultRow): GoldIntake {
        return GoldIntake(
            id = row[GoldIntakes.id],
            vendorId = row[GoldIntakes.vendorId],
            partyType = io.yavero.almasasuite.model.PartyType.fromString(row[GoldIntakes.partyType]),
            partyName = row[GoldIntakes.partyName],
            karat = row[GoldIntakes.karat],
            grams = row[GoldIntakes.grams].toDouble(),
            designFeePerGram = row[GoldIntakes.designFeePerGram].toDouble(),
            metalValuePerGram = row[GoldIntakes.metalValuePerGram].toDouble(),
            totalDesignFeePaid = row[GoldIntakes.totalDesignFeePaid].toDouble(),
            totalMetalValueOwed = row[GoldIntakes.totalMetalValueOwed].toDouble(),
            notes = row[GoldIntakes.notes],
            createdAt = row[GoldIntakes.createdAt],
            createdBy = row[GoldIntakes.createdBy],
            synced = row[GoldIntakes.synced]
        )
    }


    private fun mapRowToVendor(row: ResultRow): Vendor {
        return Vendor(
            id = row[Vendors.id],
            name = row[Vendors.name],
            contactInfo = row[Vendors.contactInfo],
            totalLiabilityBalance = row[Vendors.totalLiabilityBalance].toDouble(),
            totalPaid = row[Vendors.totalPaid].toDouble(),
            totalIntakeValue = row[Vendors.totalIntakeValue].toDouble(),
            isActive = row[Vendors.isActive],
            notes = row[Vendors.notes],
            createdAt = row[Vendors.createdAt],
            updatedAt = row[Vendors.updatedAt],
            createdBy = row[Vendors.createdBy],
            synced = row[Vendors.synced]
        )
    }


    private fun mapRowToVendorPayment(row: ResultRow): VendorPayment {
        return VendorPayment(
            id = row[VendorPayments.id],
            vendorId = row[VendorPayments.vendorId],
            amount = row[VendorPayments.amount].toDouble(),
            paymentMethod = io.yavero.almasasuite.model.VendorPaymentMethod.fromString(row[VendorPayments.paymentMethod]),
            paymentReference = row[VendorPayments.paymentReference],
            notes = row[VendorPayments.notes],
            paidAt = row[VendorPayments.paidAt],
            recordedAt = row[VendorPayments.recordedAt],
            recordedBy = row[VendorPayments.recordedBy],
            synced = row[VendorPayments.synced]
        )
    }


    suspend fun getAllVendors(): List<VendorResponse> = dbQuery {
        Vendors.selectAll()
            .where { Vendors.isActive eq true }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToVendor(it).toResponse() }
    }


    suspend fun getVendorById(id: String): VendorResponse? = dbQuery {
        Vendors.selectAll().where { Vendors.id eq id }
            .map { mapRowToVendor(it).toResponse() }
            .singleOrNull()
    }


    suspend fun createOrGetVendor(name: String, contactInfo: String? = null, createdBy: String): Vendor = dbQuery {

        val existingVendor = Vendors.selectAll()
            .where { (Vendors.name eq name) and (Vendors.isActive eq true) }
            .map { mapRowToVendor(it) }
            .singleOrNull()

        if (existingVendor != null) {
            existingVendor
        } else {

            val vendorId = UUID.randomUUID().toString()
            val now = Clock.System.now().toEpochMilliseconds()

            Vendors.insert {
                it[id] = vendorId
                it[Vendors.name] = name
                it[Vendors.contactInfo] = contactInfo
                it[totalLiabilityBalance] = 0.0.toBigDecimal()
                it[totalPaid] = 0.0.toBigDecimal()
                it[totalIntakeValue] = 0.0.toBigDecimal()
                it[isActive] = true
                it[notes] = null
                it[createdAt] = now
                it[updatedAt] = now
                it[Vendors.createdBy] = createdBy
                it[synced] = true
            }

            Vendor(
                id = vendorId,
                name = name,
                contactInfo = contactInfo,
                totalLiabilityBalance = 0.0,
                totalPaid = 0.0,
                totalIntakeValue = 0.0,
                isActive = true,
                notes = null,
                createdAt = now,
                updatedAt = now,
                createdBy = createdBy,
                synced = true
            )
        }
    }


    suspend fun recordGoldIntake(request: GoldIntakeRequest, recordedBy: String): GoldIntakeResponse = dbQuery {
        val intakeId = UUID.randomUUID().toString()
        val now = Clock.System.now().toEpochMilliseconds()


        val vendor = if (request.partyType == io.yavero.almasasuite.model.PartyType.SELLER && request.vendorId == null) {

            createOrGetVendor(request.partyName, null, recordedBy)
        } else if (request.vendorId != null) {

            Vendors.selectAll().where { Vendors.id eq request.vendorId }
                .map { mapRowToVendor(it) }
                .singleOrNull() ?: throw IllegalArgumentException("Vendor not found")
        } else {
            null
        }


        val totalDesignFeePaid = request.designFeePerGram * request.grams
        val totalMetalValueOwed = request.metalValuePerGram * request.grams


        GoldIntakes.insert {
            it[id] = intakeId
            it[vendorId] = vendor?.id
            it[partyType] = io.yavero.almasasuite.model.PartyType.toDbValue(request.partyType)
            it[partyName] = request.partyName
            it[karat] = request.karat
            it[grams] = request.grams.toBigDecimal()
            it[designFeePerGram] = request.designFeePerGram.toBigDecimal()
            it[metalValuePerGram] = request.metalValuePerGram.toBigDecimal()
            it[GoldIntakes.totalDesignFeePaid] = totalDesignFeePaid.toBigDecimal()
            it[GoldIntakes.totalMetalValueOwed] = totalMetalValueOwed.toBigDecimal()
            it[notes] = request.notes
            it[createdAt] = now
            it[createdBy] = recordedBy
            it[synced] = true
        }


        if (vendor != null && request.partyType == io.yavero.almasasuite.model.PartyType.SELLER && totalMetalValueOwed > 0) {
            val newLiability = vendor.totalLiabilityBalance + totalMetalValueOwed
            val newIntakeValue = vendor.totalIntakeValue + totalMetalValueOwed

            Vendors.update({ Vendors.id eq vendor.id }) {
                it[totalLiabilityBalance] = newLiability.toBigDecimal()
                it[totalIntakeValue] = newIntakeValue.toBigDecimal()
                it[updatedAt] = now
            }
        }


        val productId = UUID.randomUUID().toString()
        val productSku = "GOLD-${request.karat}K-${System.currentTimeMillis()}"

        Products.insert {
            it[Products.id] = productId
            it[Products.sku] = productSku
            it[Products.imageUrl] = null
            it[Products.type] = io.yavero.almasasuite.model.ProductType.toDbValue(io.yavero.almasasuite.model.ProductType.OTHER)
            it[Products.karat] = request.karat
            it[Products.weightGrams] = request.grams.toBigDecimal()
            it[Products.designFee] = totalDesignFeePaid.toBigDecimal()
            it[Products.purchasePrice] = totalMetalValueOwed.toBigDecimal()
            it[Products.quantityInStock] = 1
            it[Products.createdAt] = now
            it[Products.updatedAt] = now
        }


        GoldIntake(
            id = intakeId,
            vendorId = vendor?.id,
            partyType = request.partyType,
            partyName = request.partyName,
            karat = request.karat,
            grams = request.grams,
            designFeePerGram = request.designFeePerGram,
            metalValuePerGram = request.metalValuePerGram,
            totalDesignFeePaid = totalDesignFeePaid,
            totalMetalValueOwed = totalMetalValueOwed,
            notes = request.notes,
            createdAt = now,
            createdBy = recordedBy,
            synced = true
        ).toResponse(vendor?.name)
    }


    suspend fun recordVendorPayment(request: VendorPaymentRequest, recordedBy: String): VendorPaymentResponse = dbQuery {
        val paymentId = UUID.randomUUID().toString()
        val now = Clock.System.now().toEpochMilliseconds()
        val paidAt = request.paidAt ?: now


        val vendor = Vendors.selectAll().where { Vendors.id eq request.vendorId }
            .map { mapRowToVendor(it) }
            .singleOrNull() ?: throw IllegalArgumentException("Vendor not found")


        if (request.amount > vendor.totalLiabilityBalance) {
            throw IllegalArgumentException("Payment amount ($${String.format("%.2f", request.amount)}) exceeds vendor liability ($${String.format("%.2f", vendor.totalLiabilityBalance)})")
        }


        VendorPayments.insert {
            it[VendorPayments.id] = paymentId
            it[VendorPayments.vendorId] = request.vendorId
            it[VendorPayments.amount] = request.amount.toBigDecimal()
            it[VendorPayments.paymentMethod] = io.yavero.almasasuite.model.VendorPaymentMethod.toDbValue(request.paymentMethod)
            it[VendorPayments.paymentReference] = request.paymentReference
            it[VendorPayments.notes] = request.notes
            it[VendorPayments.paidAt] = paidAt
            it[VendorPayments.recordedAt] = now
            it[VendorPayments.recordedBy] = recordedBy
            it[VendorPayments.synced] = true
        }


        val newLiability = vendor.totalLiabilityBalance - request.amount
        val newTotalPaid = vendor.totalPaid + request.amount

        Vendors.update({ Vendors.id eq request.vendorId }) {
            it[totalLiabilityBalance] = newLiability.toBigDecimal()
            it[totalPaid] = newTotalPaid.toBigDecimal()
            it[updatedAt] = now
        }


        VendorPayment(
            id = paymentId,
            vendorId = request.vendorId,
            amount = request.amount,
            paymentMethod = request.paymentMethod,
            paymentReference = request.paymentReference,
            notes = request.notes,
            paidAt = paidAt,
            recordedAt = now,
            recordedBy = recordedBy,
            synced = true
        ).toResponse(vendor.name)
    }


    suspend fun getAllGoldIntakes(): List<GoldIntakeResponse> = dbQuery {
        GoldIntakes
            .leftJoin(Vendors)
            .selectAll()
            .orderBy(GoldIntakes.createdAt to SortOrder.DESC)
            .map { row ->
                val intake = mapRowToGoldIntake(row)
                val vendorName = row.getOrNull(Vendors.name)
                intake.toResponse(vendorName)
            }
    }


    suspend fun getVendorPayments(vendorId: String? = null): List<VendorPaymentResponse> = dbQuery {
        val query = VendorPayments
            .leftJoin(Vendors)
            .selectAll()

        val filteredQuery = if (vendorId != null) {
            query.where { VendorPayments.vendorId eq vendorId }
        } else {
            query
        }

        filteredQuery
            .orderBy(VendorPayments.paidAt to SortOrder.DESC)
            .map { row ->
                val payment = mapRowToVendorPayment(row)
                val vendorName = row[Vendors.name]
                payment.toResponse(vendorName)
            }
    }


    suspend fun getLiabilityReport(): List<VendorLiabilitySummary> = dbQuery {
        Vendors.selectAll()
            .where { (Vendors.isActive eq true) and (Vendors.totalLiabilityBalance greater 0.0.toBigDecimal()) }
            .orderBy(Vendors.totalLiabilityBalance to SortOrder.DESC)
            .map { row ->
                val vendor = mapRowToVendor(row)


                val latestIntake = GoldIntakes.selectAll()
                    .where { GoldIntakes.vendorId eq vendor.id }
                    .orderBy(GoldIntakes.createdAt to SortOrder.DESC)
                    .limit(1)
                    .map { it[GoldIntakes.createdAt] }
                    .singleOrNull()

                val latestPayment = VendorPayments.selectAll()
                    .where { VendorPayments.vendorId eq vendor.id }
                    .orderBy(VendorPayments.paidAt to SortOrder.DESC)
                    .limit(1)
                    .map { it[VendorPayments.paidAt] }
                    .singleOrNull()


                val intakeCount = GoldIntakes.selectAll()
                    .where { GoldIntakes.vendorId eq vendor.id }
                    .count().toInt()

                val paymentCount = VendorPayments.selectAll()
                    .where { VendorPayments.vendorId eq vendor.id }
                    .count().toInt()

                val paymentPercentage = if (vendor.totalIntakeValue > 0.0) {
                    (vendor.totalPaid / vendor.totalIntakeValue) * 100.0
                } else {
                    0.0
                }

                VendorLiabilitySummary(
                    id = vendor.id,
                    name = vendor.name,
                    totalLiabilityBalance = vendor.totalLiabilityBalance,
                    totalPaid = vendor.totalPaid,
                    totalIntakeValue = vendor.totalIntakeValue,
                    paymentPercentage = paymentPercentage,
                    lastIntakeDate = latestIntake,
                    lastPaymentDate = latestPayment,
                    intakeCount = intakeCount,
                    paymentCount = paymentCount
                )
            }
    }


    suspend fun reduceVendorLiabilityForSale(
        vendorId: String,
        metalValueSold: Double
    ): Boolean = dbQuery {
        val vendor = Vendors.selectAll().where { Vendors.id eq vendorId }
            .map { mapRowToVendor(it) }
            .singleOrNull() ?: return@dbQuery false

        val newLiability = maxOf(0.0, vendor.totalLiabilityBalance - metalValueSold)
        val now = Clock.System.now().toEpochMilliseconds()

        Vendors.update({ Vendors.id eq vendorId }) {
            it[totalLiabilityBalance] = newLiability.toBigDecimal()
            it[updatedAt] = now
        }

        true
    }
}