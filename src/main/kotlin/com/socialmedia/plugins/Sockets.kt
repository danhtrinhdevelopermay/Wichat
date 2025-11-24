package com.socialmedia.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class ChatMessage(
    val type: String, // "message", "typing", "online", "offline"
    val senderId: Int,
    val senderName: String,
    val recipientId: Int? = null,
    val content: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class WebRTCSignal(
    val type: String, // "offer", "answer", "ice-candidate"
    val senderId: Int,
    val recipientId: Int,
    val signal: String
)

class Connection(val session: DefaultWebSocketSession, val userId: Int, val username: String)

object ChatManager {
    private val connections = ConcurrentHashMap<Int, Connection>()
    
    fun addConnection(userId: Int, connection: Connection) {
        connections[userId] = connection
        broadcastUserStatus(userId, connection.username, "online")
    }
    
    fun removeConnection(userId: Int) {
        connections.remove(userId)?.let {
            broadcastUserStatus(userId, it.username, "offline")
        }
    }
    
    suspend fun sendMessageToUser(recipientId: Int, message: String) {
        connections[recipientId]?.session?.send(Frame.Text(message))
    }
    
    suspend fun broadcastUserStatus(userId: Int, username: String, status: String) {
        val message = ChatMessage(
            type = status,
            senderId = userId,
            senderName = username
        )
        val messageText = Json.encodeToString(message)
        connections.values.forEach {
            it.session.send(Frame.Text(messageText))
        }
    }
    
    suspend fun sendWebRTCSignal(signal: WebRTCSignal) {
        val signalText = Json.encodeToString(signal)
        connections[signal.recipientId]?.session?.send(Frame.Text(signalText))
    }
}

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
}
