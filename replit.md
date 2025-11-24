# Social Media Android App Project

## Tổng Quan
Dự án ứng dụng mạng xã hội Android với các tính năng nhắn tin, gửi media, và voice/video call.

## Cấu Trúc Dự Án
- **Backend**: Kotlin + Ktor framework (port 5000)
- **Android App**: Kotlin + Jetpack Compose
- **Database**: CockroachDB (PostgreSQL compatible)
- **Real-time**: WebSocket messaging
- **Video/Voice Call**: WebRTC (planned)

## Công Nghệ Sử Dụng
### Backend
- Ktor 2.3.7
- Exposed ORM
- PostgreSQL/CockroachDB
- JWT Authentication
- WebSocket

### Android
- Kotlin 1.9.22
- Jetpack Compose
- Material 3
- Retrofit + OkHttp
- WebRTC (Stream WebRTC Android)

## Tính Năng Đã Hoàn Thành
✅ Backend API structure
✅ Authentication (register, login)
✅ Real-time messaging với WebSocket
✅ Media upload API
✅ Android app structure
✅ Login/Register screens
✅ Chat list and detail screens
✅ GitHub Actions workflow

## Cần Làm
- [ ] Cấu hình CockroachDB connection string
- [ ] Implement WebRTC cho voice/video call
- [ ] Test end-to-end flow
- [ ] Deploy backend lên Replit

## Biến Môi Trường Cần Thiết
- DATABASE_URL: Connection string cho CockroachDB
- DB_USER: Username CockroachDB
- DB_PASSWORD: Password CockroachDB
- JWT_SECRET: Secret key cho JWT

## Ghi Chú
- Backend chạy trên port 5000
- Android app cần update BASE_URL trong RetrofitClient.kt
- WebSocket URL cần được cập nhật trong WebSocketManager.kt
