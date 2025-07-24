package io.yavero.almasasuite

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.yavero.almasasuite.config.AppConfig
import io.yavero.almasasuite.plugins.configureDatabases
import io.yavero.almasasuite.plugins.configureSecurity
import io.yavero.almasasuite.routes.authRoutes
import io.yavero.almasasuite.routes.goldIntakeRoutes
import io.yavero.almasasuite.routes.productRoutes
import io.yavero.almasasuite.routes.salesRoutes
import kotlinx.serialization.json.Json

fun main() {


    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {



    configureSecurity()
    configureDatabases()


    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }


    install(StatusPages) {

        exception<RuntimeException> { call, cause ->
            when (cause.message) {
                "Invalid or expired JWT token" -> {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        mapOf("error" to "Authentication failed", "message" to "Invalid or expired JWT token")
                    )
                }
                else -> {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Internal server error", "message" to cause.message)
                    )
                }
            }
        }


        exception<kotlinx.serialization.SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid request format", "message" to "Request body contains invalid JSON or missing required fields")
            )
        }


        exception<java.sql.SQLException> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Database error", "message" to "A database operation failed")
            )
        }


        exception<NumberFormatException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid number format", "message" to "One or more numeric values are invalid")
            )
        }


        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Invalid argument", "message" to (cause.message ?: "Invalid request parameters"))
            )
        }


        exception<Exception> { call, cause ->

            call.application.log.error("Unhandled exception", cause)

            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal server error", "message" to "An unexpected error occurred")
            )
        }
    }


    routing {

        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }


        route("/api") {

            authRoutes()


            authenticate("jwt") {
                productRoutes()
            }


            authenticate("jwt") {
                salesRoutes()
            }


            authenticate("jwt") {
                goldIntakeRoutes()
            }
        }
    }
}