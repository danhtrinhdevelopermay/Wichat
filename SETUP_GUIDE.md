# Hướng Dẫn Cài Đặt Chi Tiết

## 📋 Yêu Cầu Hệ Thống

- **JDK 17+** (OpenJDK hoặc GraalVM)
- **Android Studio** (phiên bản mới nhất)
- **Tài khoản CockroachDB** (miễn phí tại [cockroachlabs.com](https://cockroachlabs.com))
- **Git**

## 🗄️ Bước 1: Thiết Lập CockroachDB

### 1.1 Tạo Database Cluster

1. Truy cập https://cockroachlabs.com và đăng ký tài khoản miễn phí
2. Tạo cluster mới (chọn Serverless - free tier)
3. Lưu lại connection string có dạng:
   ```
   postgresql://username:password@cluster-name.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full
   ```

### 1.2 Tạo Database Schema

Kết nối vào CockroachDB và chạy các câu lệnh SQL sau:

```sql
-- Tạo bảng users
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

-- Tạo bảng messages
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

-- Tạo bảng media_files
CREATE TABLE media_files (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT NOW()
);

-- Tạo indexes để tăng performance
CREATE INDEX idx_messages_sender ON messages(sender_id);
CREATE INDEX idx_messages_recipient ON messages(recipient_id);
CREATE INDEX idx_messages_created ON messages(created_at DESC);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
```

## 🖥️ Bước 2: Chạy Backend API

### 2.1 Cài Đặt JDK

**macOS:**
```bash
brew install openjdk@17
```

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

**Windows:**
Tải và cài đặt từ https://adoptium.net/

### 2.2 Cấu Hình Biến Môi Trường

Tạo file `.env` trong thư mục gốc project:

```bash
# Database Configuration
DATABASE_URL=postgresql://your-username:your-password@your-cluster.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full
DB_USER=your_username
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-super-secret-key-change-this-in-production
JWT_ISSUER=social-media-app
JWT_REALM=social-media

# Server Configuration
PORT=5000
HOST=0.0.0.0
```

**⚠️ Quan trọng:** Thay thế các giá trị `your-*` bằng thông tin thực tế từ CockroachDB.

### 2.3 Build và Chạy Backend

```bash
# Clone project
git clone <repository-url>
cd <project-folder>

# Build project
./gradlew build

# Chạy backend server
./gradlew run
```

Backend sẽ chạy tại: `http://localhost:5000`

### 2.4 Test Backend API

Mở terminal mới và test các endpoints:

```bash
# Test health check
curl http://localhost:5000/health

# Đăng ký user mới
curl -X POST http://localhost:5000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "displayName": "Test User"
  }'

# Đăng nhập
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## 📱 Bước 3: Setup Android App

### 3.1 Mở Project trong Android Studio

1. Mở Android Studio
2. File → Open → Chọn thư mục `android/`
3. Chờ Gradle sync hoàn tất

### 3.2 Cấu Hình API URL

**File: `android/app/src/main/java/com/socialmedia/app/data/api/RetrofitClient.kt`**

Thay đổi `BASE_URL`:

```kotlin
// Nếu chạy local (emulator)
private const val BASE_URL = "http://10.0.2.2:5000/"

// Nếu chạy local (device thật)
private const val BASE_URL = "http://192.168.x.x:5000/"  // IP máy tính của bạn

// Nếu deploy lên cloud (Heroku, Railway, Replit, etc.)
private const val BASE_URL = "https://your-api.herokuapp.com/"
```

**File: `android/app/src/main/java/com/socialmedia/app/data/websocket/WebSocketManager.kt`**

Thay đổi WebSocket URL:

```kotlin
// Local
val wsUrl = "ws://10.0.2.2:5000/ws/$userId/$username"

// Cloud
val wsUrl = "wss://your-api.herokuapp.com/ws/$userId/$username"
```

### 3.3 Build và Chạy App

**Trên Emulator:**
1. Tạo AVD (Android Virtual Device) trong Android Studio
2. Click Run (hoặc Shift+F10)

**Trên Device Thật:**
1. Bật Developer Options và USB Debugging
2. Kết nối device qua USB
3. Click Run

## 🚀 Bước 4: Deploy Backend (Tùy Chọn)

### 4.1 Deploy lên Railway

1. Tạo tài khoản tại https://railway.app
2. New Project → Deploy from GitHub
3. Chọn repository
4. Add Environment Variables:
   - `DATABASE_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
5. Railway sẽ tự động build và deploy

### 4.2 Deploy lên Heroku

```bash
# Install Heroku CLI
brew install heroku/brew/heroku  # macOS
# hoặc tải từ https://devcenter.heroku.com/articles/heroku-cli

# Login
heroku login

# Tạo app
heroku create your-app-name

# Set environment variables
heroku config:set DATABASE_URL="postgresql://..."
heroku config:set JWT_SECRET="your-secret"

# Deploy
git push heroku main
```

### 4.3 Deploy lên Replit

1. Import repository vào Replit
2. Cấu hình Secrets trong Replit:
   - `DATABASE_URL`
   - `DB_USER`
   - `DB_PASSWORD`
   - `JWT_SECRET`
3. Click Run

## 🤖 Bước 5: Build APK với GitHub Actions

### 5.1 Setup Repository

1. Push code lên GitHub:
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/your-username/your-repo.git
git push -u origin main
```

### 5.2 Build APK

Workflow GitHub Actions sẽ tự động chạy khi có push/pull request.

Để download APK:
1. Vào repository trên GitHub
2. Click tab **Actions**
3. Chọn workflow run mới nhất
4. Download **Artifacts** → `app-debug.apk` hoặc `app-release.apk`

### 5.3 Sign Release APK (Production)

Để tạo signed release APK:

1. Tạo keystore:
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

2. Convert keystore sang base64:
```bash
base64 -i release-key.jks > keystore.b64
```

3. Add GitHub Secrets:
   - `KEYSTORE_BASE64`: Nội dung file keystore.b64
   - `KEY_ALIAS`: Alias của key
   - `KEY_PASSWORD`: Password của key
   - `STORE_PASSWORD`: Password của keystore

4. Workflow sẽ tự động build signed APK

## 🔧 Troubleshooting

### Lỗi kết nối Backend

**Lỗi:** `Unable to connect to backend`

**Giải pháp:**
- Kiểm tra backend đang chạy: `curl http://localhost:5000/health`
- Kiểm tra firewall không block port 5000
- Với device thật: Đảm bảo cùng WiFi với máy chạy backend

### Lỗi Database Connection

**Lỗi:** `Connection refused` hoặc `SSL error`

**Giải pháp:**
- Kiểm tra DATABASE_URL đúng format
- Kiểm tra username/password CockroachDB
- Đảm bảo có `sslmode=verify-full` trong connection string
- Kiểm tra IP whitelist trong CockroachDB Console

### Lỗi Gradle Build

**Lỗi:** `Could not resolve dependencies`

**Giải pháp:**
```bash
# Clear Gradle cache
rm -rf ~/.gradle/caches/

# Rebuild
./gradlew clean build --refresh-dependencies
```

### WebSocket Connection Failed

**Lỗi:** `WebSocket connection failed`

**Giải pháp:**
- Đảm bảo dùng `ws://` (không SSL) hoặc `wss://` (có SSL)
- Kiểm tra backend logs xem có accept WebSocket connection không
- Với HTTPS backend, phải dùng `wss://`

## 📞 Hỗ Trợ

Nếu gặp vấn đề:
1. Kiểm tra backend logs
2. Kiểm tra Android Logcat trong Android Studio
3. Xem file README.md để biết thêm chi tiết

## ✅ Checklist Hoàn Thành

- [ ] CockroachDB cluster đã tạo
- [ ] Database schema đã setup
- [ ] Backend chạy thành công (test /health endpoint)
- [ ] Android app build thành công
- [ ] Có thể đăng ký user mới
- [ ] Có thể đăng nhập
- [ ] Xem được danh sách users
- [ ] Gửi nhận tin nhắn hoạt động
- [ ] WebSocket real-time hoạt động
- [ ] GitHub Actions build APK thành công

Chúc bạn thành công! 🎉
