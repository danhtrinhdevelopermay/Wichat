package com.socialmedia.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Messages : IntIdTable("messages") {
    val senderId = reference("sender_id", Users)
    val recipientId = reference("recipient_id", Users)
    val content = text("content")
    val messageType = varchar("message_type", 20).default("text") // text, image, video, audio
    val mediaUrl = varchar("media_url", 500).nullable()
    val isRead = bool("is_read").default(false)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
}

@Serializable
data class MessageDTO(
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

@Serializable
data class SendMessageRequest(
    val recipientId: Int,
    val content: String,
    val messageType: String = "text",
    val mediaUrl: String? = null
)
