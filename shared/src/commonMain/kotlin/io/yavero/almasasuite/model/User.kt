package io.yavero.almasasuite.model

import kotlinx.serialization.Serializable


@Serializable
enum class UserRole {
    ADMIN,
    MANAGER,
    STAFF;

    companion object {
        fun fromString(value: String): UserRole {
            return when (value.uppercase()) {
                "ADMIN" -> ADMIN
                "MANAGER" -> MANAGER
                "STAFF" -> STAFF
                else -> STAFF
            }
        }

        fun toDbValue(role: UserRole): String {
            return role.name.lowercase()
        }
    }
}


@Serializable
data class User(
    val id: String,
    val name: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Long
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
    val user: User?,
    val message: String? = null
)


@Serializable
data class AuthenticatedUser(
    val id: String,
    val name: String,
    val role: UserRole
)