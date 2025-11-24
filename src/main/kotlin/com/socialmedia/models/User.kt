package com.socialmedia.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : IntIdTable("users") {
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val displayName = varchar("display_name", 100)
    val avatarUrl = varchar("avatar_url", 500).nullable()
    val status = varchar("status", 20).default("offline")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val lastSeen = datetime("last_seen").default(LocalDateTime.now())
}

@Serializable
data class UserDTO(
    val id: Int,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val status: String = "offline"
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDTO
)
