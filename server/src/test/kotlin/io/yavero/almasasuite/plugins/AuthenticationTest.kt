package io.yavero.almasasuite.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.yavero.almasasuite.plugins.configureSecurity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals


class AuthenticationTest {

    private fun Application.testModule() {

        configureSecurity()


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
        }


        routing {

            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
            }


            route("/api") {

            authenticate("jwt") {
                    get("/products") {
                        call.respond(HttpStatusCode.OK, mapOf("products" to emptyList<String>()))
                    }
                }
            }
        }
    }

    @Test
    fun `test invalid JWT token returns 401 with proper error message`() = testApplication {
        application {
            testModule()
        }


        val response = client.get("/api/products") {
            header(HttpHeaders.Authorization, "Bearer invalid_token")
        }


        assertEquals(HttpStatusCode.Unauthorized, response.status)


        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("Authentication failed", json["error"]?.jsonPrimitive?.content)
        assertEquals("Invalid or expired JWT token", json["message"]?.jsonPrimitive?.content)
    }

    @Test
    fun `test missing JWT token returns 401`() = testApplication {
        application {
            testModule()
        }


        val response = client.get("/api/products")


        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test health endpoint works without authentication`() = testApplication {
        application {
            testModule()
        }


        val response = client.get("/health")


        assertEquals(HttpStatusCode.OK, response.status)


        val responseBody = response.bodyAsText()
        val json = Json.parseToJsonElement(responseBody).jsonObject

        assertEquals("UP", json["status"]?.jsonPrimitive?.content)
    }
}