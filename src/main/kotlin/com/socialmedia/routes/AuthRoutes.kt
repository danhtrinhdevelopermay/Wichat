package com.socialmedia.routes

import com.socialmedia.models.*
import com.socialmedia.plugins.JWTConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

fun Route.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            
            val existingUser = transaction {
                Users.select { (Users.username eq request.username) or (Users.email eq request.email) }
                    .singleOrNull()
            }
            
            if (existingUser != null) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to "Username or email already exists"))
                return@post
            }
            
            val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
            
            val userId = transaction {
                Users.insert {
                    it[username] = request.username
                    it[email] = request.email
                    it[passwordHash] = passwordHash
                    it[displayName] = request.displayName
                } get Users.id
            }
            
            val token = JWTConfig.generateToken(userId.value, request.username)
            val user = UserDTO(
                id = userId.value,
                username = request.username,
                email = request.email,
                displayName = request.displayName
            )
            
            call.respond(HttpStatusCode.Created, AuthResponse(token, user))
        }
        
        post("/login") {
            val request = call.receive<LoginRequest>()
            
            val userRow = transaction {
                Users.select { Users.username eq request.username }
                    .singleOrNull()
            }
            
            if (userRow == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }
            
            val passwordHash = userRow[Users.passwordHash]
            if (!BCrypt.checkpw(request.password, passwordHash)) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                return@post
            }
            
            val userId = userRow[Users.id].value
            val username = userRow[Users.username]
            
            val token = JWTConfig.generateToken(userId, username)
            val user = UserDTO(
                id = userId,
                username = username,
                email = userRow[Users.email],
                displayName = userRow[Users.displayName],
                avatarUrl = userRow[Users.avatarUrl]
            )
            
            call.respond(AuthResponse(token, user))
        }
        
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt()
                
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@get
                }
                
                val userRow = transaction {
                    Users.select { Users.id eq userId }
                        .singleOrNull()
                }
                
                if (userRow == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                    return@get
                }
                
                val user = UserDTO(
                    id = userRow[Users.id].value,
                    username = userRow[Users.username],
                    email = userRow[Users.email],
                    displayName = userRow[Users.displayName],
                    avatarUrl = userRow[Users.avatarUrl]
                )
                
                call.respond(user)
            }
            
            get("/users") {
                val users = transaction {
                    Users.selectAll().map {
                        UserDTO(
                            id = it[Users.id].value,
                            username = it[Users.username],
                            email = it[Users.email],
                            displayName = it[Users.displayName],
                            avatarUrl = it[Users.avatarUrl],
                            status = it[Users.status]
                        )
                    }
                }
                call.respond(users)
            }
        }
    }
}
