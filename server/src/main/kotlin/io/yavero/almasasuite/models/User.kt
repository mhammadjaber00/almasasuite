package io.yavero.almasasuite.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import io.yavero.almasasuite.model.UserRole


object Users : Table("users") {
    val id = varchar("id", 36)
    val name = varchar("name", 255)
    val pin = varchar("pin", 10)
    val role = varchar("role", 20)
    val isActive = bool("is_active").default(true)
    val createdAt = long("created_at")
    val updatedAt = long("updated_at")
    val createdBy = varchar("created_by", 36).nullable()

    override val primaryKey = PrimaryKey(id)
}


data class User(
    val id: String,
    val name: String,
    val pin: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String? = null
)


@Serializable
data class PinLoginRequest(
    val pin: String
)


@Serializable
data class CreateUserRequest(
    val name: String,
    val pin: String,
    val role: UserRole
)


@Serializable
data class ResetPinRequest(
    val userId: String,
    val newPin: String
)


@Serializable
data class AuthResponse(
    val success: Boolean,
    val user: UserResponse?,
    val message: String? = null
)


@Serializable
data class UserResponse(
    val id: String,
    val name: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Long
)