package com.socialmedia.routes

import com.socialmedia.models.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*

fun Route.mediaRoutes() {
    authenticate("auth-jwt") {
        route("/api/media") {
            post("/upload") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                
                val multipart = call.receiveMultipart()
                var fileName = ""
                var fileType = ""
                var fileSize = 0L
                var savedPath = ""
                
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName ?: "unknown"
                            val fileBytes = part.streamProvider().readBytes()
                            fileSize = fileBytes.size.toLong()
                            fileType = when {
                                fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) || 
                                fileName.endsWith(".png", true) -> "image"
                                fileName.endsWith(".mp4", true) || fileName.endsWith(".mov", true) -> "video"
                                fileName.endsWith(".mp3", true) || fileName.endsWith(".wav", true) -> "audio"
                                else -> "file"
                            }
                            
                            val uploadsDir = File("uploads")
                            if (!uploadsDir.exists()) {
                                uploadsDir.mkdirs()
                            }
                            
                            val uniqueFileName = "${UUID.randomUUID()}_$fileName"
                            val file = File(uploadsDir, uniqueFileName)
                            file.writeBytes(fileBytes)
                            savedPath = "/uploads/$uniqueFileName"
                        }
                        else -> {}
                    }
                    part.dispose()
                }
                
                val mediaId = transaction {
                    MediaFiles.insert {
                        it[MediaFiles.userId] = userId
                        it[MediaFiles.fileName] = fileName
                        it[MediaFiles.fileType] = fileType
                        it[MediaFiles.fileUrl] = savedPath
                        it[MediaFiles.fileSize] = fileSize
                    } get MediaFiles.id
                }
                
                val mediaRow = transaction {
                    MediaFiles.select { MediaFiles.id eq mediaId }
                        .single()
                }
                
                val media = MediaFileDTO(
                    id = mediaRow[MediaFiles.id].value,
                    fileName = mediaRow[MediaFiles.fileName],
                    fileType = mediaRow[MediaFiles.fileType],
                    fileUrl = mediaRow[MediaFiles.fileUrl],
                    fileSize = mediaRow[MediaFiles.fileSize],
                    uploadedAt = mediaRow[MediaFiles.uploadedAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
                
                call.respond(HttpStatusCode.Created, media)
            }
            
            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asInt() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                
                val mediaFiles = transaction {
                    MediaFiles.select { MediaFiles.userId eq userId }
                        .orderBy(MediaFiles.uploadedAt to SortOrder.DESC)
                        .map {
                            MediaFileDTO(
                                id = it[MediaFiles.id].value,
                                fileName = it[MediaFiles.fileName],
                                fileType = it[MediaFiles.fileType],
                                fileUrl = it[MediaFiles.fileUrl],
                                fileSize = it[MediaFiles.fileSize],
                                uploadedAt = it[MediaFiles.uploadedAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                            )
                        }
                }
                
                call.respond(mediaFiles)
            }
        }
    }
}
