package com.socialmedia.app.data.api

import com.socialmedia.app.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    @GET("api/auth/users")
    suspend fun getUsers(): Response<List<User>>
    
    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<Message>
    
    @GET("api/messages/{userId}")
    suspend fun getMessages(@Path("userId") userId: Int): Response<List<Message>>
    
    @GET("api/messages")
    suspend fun getAllConversations(): Response<List<Message>>
    
    @Multipart
    @POST("api/media/upload")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<MediaFile>
    
    @GET("api/media")
    suspend fun getMediaFiles(): Response<List<MediaFile>>
}
