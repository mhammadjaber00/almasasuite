package io.yavero.almasasuite.routes

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.yavero.almasasuite.models.*
import io.yavero.almasasuite.model.UserRole
import io.yavero.almasasuite.plugins.dbQuery
import io.yavero.almasasuite.plugins.hashPassword
import io.yavero.almasasuite.plugins.verifyPassword
import io.yavero.almasasuite.plugins.getUserIdFromAuth
import io.yavero.almasasuite.plugins.getManagerPinHeader
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*


fun Route.authRoutes() {
    route("/auth") {

        post("/login") {
            val loginRequest = call.receive<PinLoginRequest>()


            val user = dbQuery {
                Users.selectAll().where { Users.pin eq hashPassword(loginRequest.pin) }
                    .map {
                        User(
                            id = it[Users.id],
                            name = it[Users.name],
                            pin = it[Users.pin],
                            role = UserRole.fromString(it[Users.role]),
                            isActive = it[Users.isActive],
                            createdAt = it[Users.createdAt],
                            updatedAt = it[Users.updatedAt],
                            createdBy = it[Users.createdBy]
                        )
                    }
                    .singleOrNull()
            }

            if (user != null && user.isActive && verifyPassword(loginRequest.pin, user.pin)) {
                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(
                        success = true,
                        user = UserResponse(
                            id = user.id,
                            name = user.name,
                            role = user.role,
                            isActive = user.isActive,
                            createdAt = user.createdAt
                        )
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthResponse(
                        success = false,
                        user = null,
                        message = "Invalid PIN or inactive user"
                    )
                )
            }
        }


        get("/users") {

            val users = dbQuery {
                Users.selectAll().where { Users.isActive eq true }
                    .map {
                        UserResponse(
                            id = it[Users.id],
                            name = it[Users.name],
                            role = UserRole.fromString(it[Users.role]),
                            isActive = it[Users.isActive],
                            createdAt = it[Users.createdAt]
                        )
                    }
            }
            call.respond(users)
        }


        post("/users") {
            val createRequest = call.receive<CreateUserRequest>()


            val existingUser = dbQuery {
                Users.selectAll().where { Users.pin eq hashPassword(createRequest.pin) }
                    .count() > 0
            }

            if (existingUser) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "PIN already in use"))
                return@post
            }


            val userId = UUID.randomUUID().toString()
            val now = Clock.System.now().toEpochMilliseconds()

            dbQuery {
                Users.insert {
                    it[id] = userId
                    it[name] = createRequest.name
                    it[pin] = hashPassword(createRequest.pin)
                    it[role] = UserRole.toDbValue(createRequest.role)
                    it[isActive] = true
                    it[createdAt] = now
                    it[updatedAt] = now
                    val managerPinHeader = call.request.headers[getManagerPinHeader()]
                    it[createdBy] = call.application.getUserIdFromAuth(managerPinHeader)
                }
            }


            call.respond(
                HttpStatusCode.Created,
                UserResponse(
                    id = userId,
                    name = createRequest.name,
                    role = createRequest.role,
                    isActive = true,
                    createdAt = now
                )
            )
        }


        post("/users/reset-pin") {
            val resetRequest = call.receive<ResetPinRequest>()


            val existingUser = dbQuery {
                Users.selectAll().where {
                    (Users.pin eq hashPassword(resetRequest.newPin)) and (Users.id neq resetRequest.userId)
                }.count() > 0
            }

            if (existingUser) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "PIN already in use"))
                return@post
            }


            val now = Clock.System.now().toEpochMilliseconds()
            val updated = dbQuery {
                Users.update({ Users.id eq resetRequest.userId }) {
                    it[pin] = hashPassword(resetRequest.newPin)
                    it[updatedAt] = now
                } > 0
            }

            if (updated) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "PIN reset successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }


        post("/users/{id}/deactivate") {
            val userId = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing user ID")
            )

            val now = Clock.System.now().toEpochMilliseconds()
            val updated = dbQuery {
                Users.update({ Users.id eq userId }) {
                    it[isActive] = false
                    it[updatedAt] = now
                } > 0
            }

            if (updated) {
                call.respond(HttpStatusCode.OK, mapOf("message" to "User deactivated successfully"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }
        }
    }
}