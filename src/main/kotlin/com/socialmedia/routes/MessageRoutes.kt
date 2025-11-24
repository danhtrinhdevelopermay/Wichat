package com.socialmedia.routes

import com.socialmedia.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.format.DateTimeFormatter

fun Route.messageRoutes() {
    authenticate("auth-jwt") {
        route("/api/messages") {
            post {
                val principal = call.principal<JWTPrincipal>()
                val senderId = principal?.payload?.getClaim("userId")?.asInt() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                
                val request = call.receive<SendMessageRequest>()
                
                val messageId = transaction {
                    Messages.insert {
                        it[Messages.senderId] = senderId
                        it[Messages.recipientId] = request.recipientId
                        it[content] = request.content
                        it[messageType] = request.messageType
                        it[mediaUrl] = request.mediaUrl
                    } get Messages.id
                }
                
                val messageRow = transaction {
                    (Messages innerJoin Users)
                        .slice(Messages.columns + Users.displayName)
                        .select { Messages.id eq messageId }
                        .single()
                }
                
                val message = MessageDTO(
                    id = messageRow[Messages.id].value,
                    senderId = messageRow[Messages.senderId].value,
                    senderName = messageRow[Users.displayName],
                    recipientId = messageRow[Messages.recipientId].value,
                    content = messageRow[Messages.content],
                    messageType = messageRow[Messages.messageType],
                    mediaUrl = messageRow[Messages.mediaUrl],
                    isRead = messageRow[Messages.isRead],
                    createdAt = messageRow[Messages.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
                
                call.respond(HttpStatusCode.Created, message)
            }
            
            get("/{userId}") {
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.payload?.getClaim("userId")?.asInt() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                
                val otherUserId = call.parameters["userId"]?.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID"))
                    return@get
                }
                
                val messages = transaction {
                    (Messages innerJoin Users)
                        .slice(Messages.columns + Users.displayName)
                        .select {
                            ((Messages.senderId eq currentUserId) and (Messages.recipientId eq otherUserId)) or
                            ((Messages.senderId eq otherUserId) and (Messages.recipientId eq currentUserId))
                        }
                        .orderBy(Messages.createdAt to SortOrder.ASC)
                        .map {
                            MessageDTO(
                                id = it[Messages.id].value,
                                senderId = it[Messages.senderId].value,
                                senderName = it[Users.displayName],
                                recipientId = it[Messages.recipientId].value,
                                content = it[Messages.content],
                                messageType = it[Messages.messageType],
                                mediaUrl = it[Messages.mediaUrl],
                                isRead = it[Messages.isRead],
                                createdAt = it[Messages.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )
                        }
                }
                
                call.respond(messages)
            }
            
            get {
                val principal = call.principal<JWTPrincipal>()
                val currentUserId = principal?.payload?.getClaim("userId")?.asInt() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                
                val conversations = transaction {
                    (Messages innerJoin Users)
                        .slice(Messages.columns + Users.displayName)
                        .select {
                            (Messages.senderId eq currentUserId) or (Messages.recipientId eq currentUserId)
                        }
                        .orderBy(Messages.createdAt to SortOrder.DESC)
                        .map {
                            MessageDTO(
                                id = it[Messages.id].value,
                                senderId = it[Messages.senderId].value,
                                senderName = it[Users.displayName],
                                recipientId = it[Messages.recipientId].value,
                                content = it[Messages.content],
                                messageType = it[Messages.messageType],
                                mediaUrl = it[Messages.mediaUrl],
                                isRead = it[Messages.isRead],
                                createdAt = it[Messages.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )
                        }
                }
                
                call.respond(conversations)
            }
        }
    }
}
