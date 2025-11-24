package com.socialmedia.plugins

import com.socialmedia.routes.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Social Media API is running!")
        }
        
        get("/health") {
            call.respondText("OK")
        }
        
        authRoutes()
        messageRoutes()
        mediaRoutes()
        webSocketRoutes()
    }
}
