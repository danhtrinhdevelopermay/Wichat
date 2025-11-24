# Social Media Android App

Ứng dụng mạng xã hội nhắn tin với các tính năng đầy đủ, được xây dựng bằng Kotlin cho Android và backend API bằng Ktor.

## ✨ Tính Năng

### 📱 Android App
- ✅ Đăng ký và đăng nhập người dùng
- ✅ Danh sách người dùng
- ✅ Nhắn tin real-time qua WebSocket
- ✅ Gửi ảnh, video (upload media)
- 🚧 Gọi thoại (WebRTC)
- 🚧 Gọi video (WebRTC)
- 🎨 Giao diện hiện đại với Jetpack Compose
- 🔐 Xác thực JWT

### 🖥️ Backend API
- ✅ RESTful API với Ktor
- ✅ Kết nối CockroachDB (PostgreSQL compatible)
- ✅ WebSocket cho real-time messaging
- ✅ JWT authentication
- ✅ Upload và quản lý media files
- ✅ API endpoints đầy đủ

## 🚀 Cài Đặt

### Yêu Cầu
- JDK 17+
- Android Studio (cho phát triển Android)
- CockroachDB account (từ cockroachlabs.com)

### Backend Setup

1. **Cấu hình biến môi trường:**
```bash
export DATABASE_URL="jdbc:postgresql://your-cluster.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full"
export DB_USER="your_username"
export DB_PASSWORD="your_password"
export JWT_SECRET="your-secret-key"
```

2. **Chạy backend:**
```bash
./gradlew run
```

Backend sẽ chạy trên `http://0.0.0.0:5000`

### Android App Setup

1. Mở thư mục `android/` trong Android Studio

2. Cập nhật URL API trong `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "https://your-api-url.repl.co/"
```

3. Cập nhật WebSocket URL trong `WebSocketManager.kt`:
```kotlin
val wsUrl = "wss://your-api-url.repl.co/ws/$userId/$username"
```

4. Build và chạy app trên emulator hoặc thiết bị thật

## 🔧 Cấu Trúc Dự Án

```
.
├── android/                    # Android app
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/socialmedia/app/
│   │   │   │   │   ├── data/          # Models, API, WebSocket
│   │   │   │   │   ├── ui/            # UI screens, ViewModels
│   │   │   │   │   └── MainActivity.kt
│   │   │   │   ├── AndroidManifest.xml
│   │   │   │   └── res/
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
├── src/                        # Backend Kotlin/Ktor
│   ├── main/
│   │   ├── kotlin/com/socialmedia/
│   │   │   ├── models/        # Data models
│   │   │   ├── plugins/       # Ktor plugins
│   │   │   ├── routes/        # API routes
│   │   │   └── Application.kt
│   │   └── resources/
│   └── test/
│
├── .github/
│   └── workflows/
│       └── android-build.yml  # GitHub Actions workflow
│
└── build.gradle.kts           # Backend build config
```

## 📡 API Endpoints

### Authentication
- `POST /api/auth/register` - Đăng ký người dùng mới
- `POST /api/auth/login` - Đăng nhập
- `GET /api/auth/me` - Lấy thông tin người dùng hiện tại
- `GET /api/auth/users` - Lấy danh sách người dùng

### Messages
- `POST /api/messages` - Gửi tin nhắn
- `GET /api/messages/{userId}` - Lấy tin nhắn với một người dùng
- `GET /api/messages` - Lấy tất cả cuộc hội thoại

### Media
- `POST /api/media/upload` - Upload file (ảnh, video)
- `GET /api/media` - Lấy danh sách media files

### WebSocket
- `WS /ws/{userId}/{username}` - Kết nối WebSocket cho real-time messaging

## 🤖 GitHub Actions

Dự án sử dụng GitHub Actions để tự động build APK khi có push/pull request.

### Build APK Locally
```bash
cd android
./gradlew assembleDebug
```

APK sẽ được tạo tại: `android/app/build/outputs/apk/debug/app-debug.apk`

### Build APK trên GitHub
1. Push code lên GitHub
2. Vào tab "Actions"
3. Chọn workflow "Build Android APK"
4. Download APK từ artifacts

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'offline',
    created_at TIMESTAMP DEFAULT NOW(),
    last_seen TIMESTAMP DEFAULT NOW()
);
```

### Messages Table
```sql
CREATE TABLE messages (
    id SERIAL PRIMARY KEY,
    sender_id INT REFERENCES users(id),
    recipient_id INT REFERENCES users(id),
    content TEXT NOT NULL,
    message_type VARCHAR(20) DEFAULT 'text',
    media_url VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### Media Files Table
```sql
CREATE TABLE media_files (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT NOW()
);
```

## 🔐 Bảo Mật

- Mật khẩu được hash bằng BCrypt
- JWT tokens cho authentication
- HTTPS/WSS cho production
- Input validation
- SQL injection protection qua Exposed ORM

## 📝 License

MIT License

## 🤝 Đóng Góp

Contributions, issues và feature requests đều được chào đón!

## 👨‍💻 Tác Giả

Dự án được tạo bởi Replit Agent
