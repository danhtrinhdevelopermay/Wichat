package com.socialmedia.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object MediaFiles : IntIdTable("media_files") {
    val userId = reference("user_id", Users)
    val fileName = varchar("file_name", 255)
    val fileType = varchar("file_type", 50) // image, video, audio
    val fileUrl = varchar("file_url", 500)
    val fileSize = long("file_size")
    val uploadedAt = datetime("uploaded_at").default(LocalDateTime.now())
}

@Serializable
data class MediaFileDTO(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileUrl: String,
    val fileSize: Long,
    val uploadedAt: String
)
