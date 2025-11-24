package com.socialmedia

import com.socialmedia.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 5000, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDatabase()
    configureSecurity()
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureSockets()
    configureRouting()
}
