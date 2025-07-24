package io.yavero.almasasuite.config

import io.ktor.server.application.*
import io.ktor.server.config.*


class AppConfig(private val config: ApplicationConfig) {


    val database = DatabaseConfig(config.config("db"))


    val jwt = JwtConfig(config.config("jwt"))


    val security = SecurityConfig(config.config("security"))


    val server = KtorServerConfig(config.config("ktor.deployment"))


    class DatabaseConfig(config: ApplicationConfig) {
        val url: String = config.propertyOrNull("url")?.getString() ?: "jdbc:postgresql://localhost:5432/almasa"
        val user: String = config.propertyOrNull("user")?.getString() ?: "postgres"
        val password: String = config.propertyOrNull("password")?.getString() ?: "postgres"
        val driver: String = config.propertyOrNull("driver")?.getString() ?: "org.postgresql.Driver"
    }


    class JwtConfig(config: ApplicationConfig) {
        val secret: String = config.propertyOrNull("secret")?.getString() ?: "dev-jwt-secret-do-not-use-in-production"
        val issuer: String = config.propertyOrNull("issuer")?.getString() ?: "almasa-suite"
        val audience: String = config.propertyOrNull("audience")?.getString() ?: "almasa-users"
        val realm: String = config.propertyOrNull("realm")?.getString() ?: "Almasa Suite"
    }


    class SecurityConfig(config: ApplicationConfig) {
        // Manager pin functionality has been removed
    }


    class KtorServerConfig(private val config: ApplicationConfig) {
        val port: Int = config.propertyOrNull("port")?.getString()?.toIntOrNull() ?: 8080
        val host: String = config.propertyOrNull("host")?.getString() ?: "0.0.0.0"
    }

    companion object {


        fun from(environment: ApplicationEnvironment): AppConfig {
            return AppConfig(environment.config)
        }


        fun from(config: ApplicationConfig): AppConfig {
            return AppConfig(config)
        }
    }
}


val Application.appConfig: AppConfig
    get() = AppConfig.from(environment)