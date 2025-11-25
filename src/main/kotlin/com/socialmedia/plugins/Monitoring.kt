package com.socialmedia.plugins

import io.ktor.server.application.*

fun Application.configureMonitoring() {
    // Monitoring configured via logback.xml
    log.info("Monitoring configured")
}
