package com.socialmedia.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import com.socialmedia.models.*

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable"
    val dbUser = System.getenv("DB_USER") ?: "root"
    val dbPassword = System.getenv("DB_PASSWORD") ?: ""
    
    // Add jdbc: prefix if not present and fix SSL mode
    var jdbcUrl = if (dbUrl.startsWith("postgresql://")) {
        "jdbc:$dbUrl"
    } else {
        dbUrl
    }
    
    // Replace sslmode=verify-full with sslmode=require (doesn't need cert file)
    jdbcUrl = jdbcUrl.replace("sslmode=verify-full", "sslmode=require")
    
    val config = HikariConfig().apply {
        this.jdbcUrl = jdbcUrl
        username = dbUser
        password = dbPassword
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }
    
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    
    transaction {
        SchemaUtils.create(Users, Messages, MediaFiles)
    }
    
    log.info("Database configured successfully")
}
