package com.socialmedia.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val status: String = "offline"
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val displayName: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val token: String,
    val user: User
)

data class Message(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val recipientId: Int,
    val content: String,
    val messageType: String = "text",
    val mediaUrl: String? = null,
    val isRead: Boolean = false,
    val createdAt: String
)

data class SendMessageRequest(
    val recipientId: Int,
    val content: String,
    val messageType: String = "text",
    val mediaUrl: String? = null
)

data class MediaFile(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileUrl: String,
    val fileSize: Long,
    val uploadedAt: String
)

data class ChatMessage(
    val type: String,
    val senderId: Int,
    val senderName: String,
    val recipientId: Int? = null,
    val content: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class WebRTCSignal(
    val type: String,
    val senderId: Int,
    val recipientId: Int,
    val signal: String
)
