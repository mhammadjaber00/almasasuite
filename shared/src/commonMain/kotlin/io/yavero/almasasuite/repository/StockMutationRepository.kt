package io.yavero.almasasuite.repository

import io.yavero.almasasuite.db.AlmasaDatabase
import io.yavero.almasasuite.db.StockMutation as DbStockMutation
import io.yavero.almasasuite.model.StockMutation
import io.yavero.almasasuite.model.StockMutationReason
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.random.Random


interface StockMutationRepository {


    fun getAllStockMutations(): Flow<List<StockMutation>>


    suspend fun getStockMutationById(id: String): StockMutation?


    suspend fun getStockMutationsByProductId(productId: String): List<StockMutation>


    suspend fun getStockMutationsByReason(reason: StockMutationReason): List<StockMutation>


    suspend fun getStockMutationsByDateRange(start: Instant, end: Instant): List<StockMutation>


    suspend fun getUnsyncedStockMutations(): List<StockMutation>


    suspend fun createStockMutation(
        productId: String,
        delta: Int,
        reason: String,
        timestamp: Instant = Clock.System.now()
    ): Boolean


    suspend fun markStockMutationAsSynced(id: String): Boolean


    suspend fun deleteStockMutation(id: String): Boolean
}


class StockMutationRepositoryImpl(
    private val database: AlmasaDatabase
) : StockMutationRepository {

    override fun getAllStockMutations(): Flow<List<StockMutation>> {
        return flow {
            val mutations = database.stockMutationQueries
                .getAllStockMutations()
                .executeAsList()
                .map { it.toStockMutation() }
            emit(mutations)
        }
    }

    override suspend fun getStockMutationById(id: String): StockMutation? = database.stockMutationQueries
        .getStockMutationById(id)
        .executeAsOneOrNull()
        ?.toStockMutation()

    override suspend fun getStockMutationsByProductId(productId: String): List<StockMutation> = database.stockMutationQueries
        .getStockMutationsByProductId(productId)
        .executeAsList()
        .map { it.toStockMutation() }

    override suspend fun getStockMutationsByReason(reason: StockMutationReason): List<StockMutation> = database.stockMutationQueries
        .getStockMutationsByReason(reason.name)
        .executeAsList()
        .map { it.toStockMutation() }

    override suspend fun getStockMutationsByDateRange(start: Instant, end: Instant): List<StockMutation> {
        val startMillis = start.toEpochMilliseconds()
        val endMillis = end.toEpochMilliseconds()

        return database.stockMutationQueries
            .getStockMutationsByDateRange(startMillis, endMillis)
            .executeAsList()
            .map { it.toStockMutation() }
    }

    override suspend fun getUnsyncedStockMutations(): List<StockMutation> = database.stockMutationQueries
        .getUnsyncedStockMutations()
        .executeAsList()
        .map { it.toStockMutation() }

    override suspend fun createStockMutation(
        productId: String,
        delta: Int,
        reason: String,
        timestamp: Instant
    ): Boolean {
        return try {
            val id = generateId()
            val now = Clock.System.now().toEpochMilliseconds()

            database.stockMutationQueries.insertStockMutation(
                id = id,
                product_id = productId,
                delta = delta.toLong(),
                reason = reason,
                timestamp = timestamp.toEpochMilliseconds(),
                created_at = now,
                synced = 0L
            )
            true
        } catch (e: Exception) {

            false
        }
    }

    override suspend fun markStockMutationAsSynced(id: String): Boolean {
        return try {
            database.stockMutationQueries.markStockMutationAsSynced(id)
            true
        } catch (e: Exception) {

            false
        }
    }

    override suspend fun deleteStockMutation(id: String): Boolean {
        return try {
            database.stockMutationQueries.deleteStockMutation(id)
            true
        } catch (e: Exception) {

            false
        }
    }


    private fun DbStockMutation.toStockMutation(): StockMutation {
        return StockMutation(
            id = id,
            productId = product_id,
            delta = delta.toInt(),
            reason = StockMutationReason.valueOf(reason),
            timestamp = Instant.fromEpochMilliseconds(timestamp)
        )
    }


    private fun generateId(): String {
        return "sm_${Random.nextInt(100000, 999999)}_${Clock.System.now().toEpochMilliseconds()}"
    }
}