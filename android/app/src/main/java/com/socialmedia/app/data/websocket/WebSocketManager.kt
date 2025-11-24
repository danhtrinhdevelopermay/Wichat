package com.socialmedia.app.data.websocket

import com.google.gson.Gson
import com.socialmedia.app.data.model.ChatMessage
import okhttp3.*
import okio.ByteString

class WebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val gson = Gson()
    
    fun connect(userId: Int, username: String, onMessageReceived: (ChatMessage) -> Unit) {
        val wsUrl = "wss://your-api-url.repl.co/ws/$userId/$username" // Thay đổi URL này
        val request = Request.Builder()
            .url(wsUrl)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connected")
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, ChatMessage::class.java)
                    onMessageReceived(message)
                } catch (e: Exception) {
                    println("Error parsing message: ${e.message}")
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Received bytes: ${bytes.hex()}")
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                println("WebSocket closing: $code / $reason")
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket error: ${t.message}")
            }
        })
    }
    
    fun sendMessage(message: ChatMessage) {
        val messageJson = gson.toJson(message)
        webSocket?.send(messageJson)
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Goodbye")
    }
}
