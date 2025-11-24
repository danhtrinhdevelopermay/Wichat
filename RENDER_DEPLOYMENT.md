# 🚀 Deploy Backend lên Render.com

## Tại Sao Chọn Render?

✅ **Free tier tốt hơn Replit** cho backend Kotlin  
✅ **Hỗ trợ Docker** native  
✅ **Auto-deploy** khi push lên GitHub  
✅ **SSL/HTTPS** tự động miễn phí  
✅ **PostgreSQL miễn phí** (nếu muốn thay CockroachDB)  

## 📋 Chuẩn Bị

1. **Tài khoản GitHub** (code đã push lên)
2. **Tài khoản Render.com** (miễn phí, không cần thẻ)
3. **CockroachDB connection string** (hoặc dùng Postgres của Render)

## 🔧 Bước 1: Chuẩn Bị Code

### 1.1 Build Configuration

File `build.gradle.kts` đã được cấu hình để build fat JAR. Kiểm tra có đoạn này:

```kotlin
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.socialmedia.ApplicationKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
```

### 1.2 Port Configuration

File `src/main/kotlin/com/socialmedia/Application.kt` đã được cấu hình để sử dụng PORT từ environment:

```kotlin
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 5000
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
```

✅ **Đã có trong project** - Không cần thay đổi gì!

### 1.3 Files Cần Thiết

Đã được tạo sẵn:
- ✅ `Dockerfile` - Container configuration
- ✅ `render.yaml` - Infrastructure as Code (optional)
- ✅ `.gitignore` - Không commit sensitive data

## 🗄️ Bước 2: Lựa Chọn Database

### Option A: Dùng CockroachDB (Khuyến nghị)

**Ưu điểm:**
- Global distribution
- Compatible với PostgreSQL
- Free tier vĩnh viễn (không expire)
- Lên đến 5GB storage miễn phí

**Setup:**
1. Vào https://cockroachlabs.com
2. Tạo cluster miễn phí
3. Lưu connection string:
   ```
   postgresql://user:pass@cluster.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full
   ```

### Option B: Dùng Render PostgreSQL

**Ưu điểm:**
- Tích hợp ngay trong Render
- Setup nhanh

**Nhược điểm:**
- ⚠️ Free tier **expire sau 30 ngày**
- Chỉ 1GB storage
- Phải upgrade hoặc mất data

**Setup:**
1. Trong Render Dashboard → New + → PostgreSQL
2. Chọn Free tier
3. Sau khi tạo, copy **Internal Database URL**

## 🚀 Bước 3: Deploy lên Render

### 3.1 Tạo Web Service

1. **Đăng nhập** https://dashboard.render.com
2. Click **"New +"** → **"Web Service"**
3. **Connect Repository:**
   - Click "Connect GitHub" hoặc "Connect GitLab"
   - Authorize Render
   - Chọn repository của bạn

### 3.2 Cấu Hình Service

**Service Name:** `social-media-backend` (hoặc tên bạn muốn)

**Environment:** Docker

**Region:** Oregon (hoặc Singapore nếu ở châu Á)

**Branch:** `main` (hoặc branch bạn đang dùng)

**Build & Start Commands:** 
- Để trống (Dockerfile xử lý)

### 3.3 Cấu Hình Environment Variables

Click **"Advanced"** → **"Add Environment Variable"**

Thêm các biến sau:

```bash
# Database (CockroachDB)
DATABASE_URL=postgresql://user:pass@cluster.cockroachlabs.cloud:26257/defaultdb?sslmode=verify-full
DB_USER=your_username
DB_PASSWORD=your_password

# JWT Security
JWT_SECRET=your-super-secret-key-min-32-characters-long
JWT_ISSUER=social-media-app
JWT_REALM=social-media

# Server (Render tự set PORT, nhưng có thể force)
# PORT=5000  # Optional, Render default là 10000
```

**⚠️ Quan trọng:** 
- Thay `your_username`, `your_password` bằng thông tin thật
- `JWT_SECRET` phải mạnh (ít nhất 32 ký tự)

### 3.4 Deploy

1. Click **"Create Web Service"**
2. Render sẽ:
   - Clone repository
   - Build Docker image
   - Deploy container
   - Expose public URL

## 📊 Bước 4: Kiểm Tra Deployment

### 4.1 Monitor Build Logs

Trong Render Dashboard:
- Tab **"Logs"** → Xem build progress
- Chờ thông báo **"Deploy live"** (màu xanh)

### 4.2 Test API

Sau khi deploy thành công, bạn sẽ có URL dạng:
```
https://social-media-backend-xxxx.onrender.com
```

**Test endpoints:**

```bash
# Health check
curl https://your-app-name.onrender.com/health

# Register user
curl -X POST https://your-app-name.onrender.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com", 
    "password": "password123",
    "displayName": "Test User"
  }'

# Login
curl -X POST https://your-app-name.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## 📱 Bước 5: Update Android App

Trong Android app, update API URLs:

**File:** `android/app/src/main/java/com/socialmedia/app/data/api/RetrofitClient.kt`

```kotlin
object RetrofitClient {
    // Thay đổi từ local sang Render URL
    private const val BASE_URL = "https://your-app-name.onrender.com/"
    
    // ...rest of code
}
```

**File:** `android/app/src/main/java/com/socialmedia/app/data/websocket/WebSocketManager.kt`

```kotlin
fun connect(userId: Int, username: String, onMessageReceived: (ChatMessage) -> Unit) {
    // Thay đổi từ ws:// sang wss:// (secure WebSocket)
    val wsUrl = "wss://your-app-name.onrender.com/ws/$userId/$username"
    
    // ...rest of code
}
```

**⚠️ Lưu ý:** Render dùng HTTPS/WSS (secure), không phải HTTP/WS!

## 🔄 Auto-Deploy

Mỗi khi bạn push code mới lên GitHub branch `main`, Render sẽ:
1. Tự động detect changes
2. Build lại Docker image
3. Deploy phiên bản mới

**Theo dõi:** Dashboard → Tab "Events"

## 🐛 Troubleshooting

### Lỗi: "Build Failed"

**Kiểm tra:**
- Dockerfile có đúng không
- Gradle build thành công locally: `./gradlew buildFatJar`
- Check build logs trong Render

**Fix:**
```bash
# Test build locally
docker build -t social-media-backend .
docker run -p 5000:5000 social-media-backend
```

### Lỗi: "Application Error" sau deploy

**Kiểm tra Logs:**
- Render Dashboard → Logs tab
- Tìm lỗi database connection, port binding

**Common issues:**
- DATABASE_URL sai format
- Missing environment variables
- Port không bind 0.0.0.0

### Lỗi: Database Connection Timeout

**Nếu dùng CockroachDB:**
- Kiểm tra connection string có `sslmode=verify-full`
- Verify username/password đúng
- Check IP whitelist trong CockroachDB console

**Nếu dùng Render Postgres:**
- Đảm bảo database và web service cùng region
- Dùng **Internal URL** (không phải External URL)

### Free Tier "Sleeps"

⚠️ **Render free tier sleep sau 15 phút không hoạt động**

**Giải pháp:**
1. **Upgrade lên paid tier** ($7/month) - Always on
2. **Dùng uptime monitor** (UptimeRobot, cron-job.org) ping mỗi 10 phút
3. **Chấp nhận cold start** (lần đầu load chậm 30-50 giây)

## 💰 Chi Phí

### Free Tier
- **Web Service:** Free (sleeps after inactivity)
- **PostgreSQL:** Free (expire sau 30 ngày)
- **Bandwidth:** 100GB/month
- **Build minutes:** 500 hours/month

### Paid Plans (Nếu cần)
- **Starter Web Service:** $7/month (always on)
- **Starter PostgreSQL:** $7/month (không expire, 1GB)
- **Professional:** $25/month (2GB RAM, auto-scaling)

## 🎯 Best Practices

1. **Sử dụng CockroachDB** (free vĩnh viễn) thay vì Render Postgres
2. **Set Secrets đúng cách** - Không commit vào Git
3. **Enable Auto-Deploy** để CI/CD tự động
4. **Monitor logs** thường xuyên
5. **Setup custom domain** (nếu cần) trong Settings

## 🔗 URLs Sau Khi Deploy

Lưu lại các URLs này:

```
Backend API: https://your-app-name.onrender.com
Health Check: https://your-app-name.onrender.com/health
WebSocket: wss://your-app-name.onrender.com/ws/{userId}/{username}

Render Dashboard: https://dashboard.render.com/web/srv-xxxxx
GitHub Repo: https://github.com/your-username/your-repo
CockroachDB Console: https://cockroachlabs.cloud
```

## 📚 Resources

- [Render Docs](https://render.com/docs)
- [Render + Kotlin/Ktor Guide](https://community.render.com/t/kotlin-ktor-apps-on-render-com/39102)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)

---

## ✅ Checklist Deploy

- [ ] Code đã push lên GitHub
- [ ] CockroachDB cluster đã tạo và có connection string
- [ ] Dockerfile đã có trong project
- [ ] Render account đã tạo
- [ ] Web Service đã tạo và connected với GitHub
- [ ] Environment variables đã set đầy đủ
- [ ] Build thành công (check logs)
- [ ] Test API endpoints hoạt động
- [ ] Update URL trong Android app
- [ ] Test end-to-end flow từ Android app

Chúc bạn deploy thành công! 🎉🚀
