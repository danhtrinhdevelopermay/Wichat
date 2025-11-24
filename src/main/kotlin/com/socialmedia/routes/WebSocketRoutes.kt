package com.socialmedia.routes

import com.socialmedia.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

fun Route.webSocketRoutes() {
    webSocket("/ws/{userId}/{username}") {
        val userId = call.parameters["userId"]?.toIntOrNull() ?: run {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid user ID"))
            return@webSocket
        }
        val username = call.parameters["username"] ?: run {
            close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Invalid username"))
            return@webSocket
        }
        
        val connection = Connection(this, userId, username)
        ChatManager.addConnection(userId, connection)
        
        try {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        try {
                            if (text.contains("\"type\":\"offer\"") || 
                                text.contains("\"type\":\"answer\"") || 
                                text.contains("\"type\":\"ice-candidate\"")) {
                                val signal = Json.decodeFromString<WebRTCSignal>(text)
                                ChatManager.sendWebRTCSignal(signal)
                            } else {
                                val message = Json.decodeFromString<ChatMessage>(text)
                                if (message.recipientId != null) {
                                    ChatManager.sendMessageToUser(message.recipientId, text)
                                }
                            }
                        } catch (e: Exception) {
                            println("Error processing message: ${e.message}")
                        }
                    }
                    else -> {}
                }
            }
        } catch (e: Exception) {
            println("WebSocket error: ${e.message}")
        } finally {
            ChatManager.removeConnection(userId)
        }
    }
}
