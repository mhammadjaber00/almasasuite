package io.yavero.almasasuite.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import java.sql.SQLException
import kotlin.test.assertEquals


class StatusPagesTest {

    private fun Application.testModule() {

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
            get("/test/serialization") {
                throw kotlinx.serialization.SerializationException("Invalid JSON format")
            }

            get("/test/sql") {
                throw SQLException("Database connection failed")
            }

            get("/test/number-format") {
                throw NumberFormatException("Invalid number: abc")
            }

            get("/test/illegal-argument") {
                throw IllegalArgumentException("Invalid parameter value")
            }

            get("/test/illegal-argument-null") {
                throw IllegalArgumentException()
            }

            get("/test/generic-exception") {
                throw Exception("Some unexpected error")
            }

            get("/test/runtime-jwt") {
                throw RuntimeException("Invalid or expired JWT token")
            }

            get("/test/runtime-other") {
                throw RuntimeException("Some other runtime error")
            }
        }
    }

    @Test
    fun `test SerializationException returns 400 with proper error message`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/serialization")

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Invalid request format", json["error"]?.jsonPrimitive?.content)
        assertEquals("Request body contains invalid JSON or missing required fields", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test SQLException returns 500 with proper error message`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/sql")

        assertEquals(HttpStatusCode.InternalServerError, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Database error", json["error"]?.jsonPrimitive?.content)
        assertEquals("A database operation failed", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test NumberFormatException returns 400 with proper error message`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/number-format")

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Invalid number format", json["error"]?.jsonPrimitive?.content)
        assertEquals("One or more numeric values are invalid", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test IllegalArgumentException returns 400 with custom message`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/illegal-argument")

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Invalid argument", json["error"]?.jsonPrimitive?.content)
        assertEquals("Invalid parameter value", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test IllegalArgumentException with null message returns 400 with default message`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/illegal-argument-null")

        assertEquals(HttpStatusCode.BadRequest, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Invalid argument", json["error"]?.jsonPrimitive?.content)
        assertEquals("Invalid request parameters", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test generic Exception returns 500 with proper error message`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/generic-exception")

        assertEquals(HttpStatusCode.InternalServerError, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Internal server error", json["error"]?.jsonPrimitive?.content)
        assertEquals("An unexpected error occurred", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test RuntimeException with JWT message returns 401`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/runtime-jwt")

        assertEquals(HttpStatusCode.Unauthorized, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Authentication failed", json["error"]?.jsonPrimitive?.content)
        assertEquals("Invalid or expired JWT token", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test RuntimeException with other message returns 500`() = testApplication {
        application {
            testModule()
        }

        val response = client.get("/test/runtime-other")

        assertEquals(HttpStatusCode.InternalServerError, response.status)

        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Internal server error", json["error"]?.jsonPrimitive?.content)
        assertEquals("Some other runtime error", json["message"]?.jsonPrimitive?.content)
    }
}