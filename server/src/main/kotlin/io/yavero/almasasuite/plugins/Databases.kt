package io.yavero.almasasuite.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import io.yavero.almasasuite.config.appConfig


fun Application.configureDatabases() {
    val dbCfg = appConfig.database


    runFlyway(dbCfg.url, dbCfg.user, dbCfg.password)


    try {
        Database.connect(
            url = dbCfg.url,
            driver = dbCfg.driver,
            user = dbCfg.user,
            password = dbCfg.password
        )
        log.info("Connected to PostgreSQL database: ${dbCfg.url}")
    } catch (e: Exception) {
        log.error("DB connection failed: ${e.message}", e)
        throw e
    }
}


private fun runFlyway(url: String, user: String, password: String) {
    val flyway = Flyway.configure()
        .dataSource(url, user, password)
        .locations("classpath:db/migration")
        .baselineOnMigrate(true)
        .load()

    val result = flyway.migrate()
    if (result.migrationsExecuted > 0) {
        println("Applied ${result.migrationsExecuted} migrations")
    } else {
        println("No new migrations")
    }
}


suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
