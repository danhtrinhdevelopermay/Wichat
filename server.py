#!/usr/bin/env python3
"""
Simple documentation server for Social Media Android App project
"""
import http.server
import socketserver
import os
from pathlib import Path

PORT = 5000
DIRECTORY = "."

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)
    
    def do_GET(self):
        if self.path == '/':
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            
            html = """
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Social Media Android App - Project Documentation</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        .container {
            background: white;
            border-radius: 12px;
            padding: 40px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        h1 {
            color: #667eea;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
        }
        h2 {
            color: #764ba2;
            margin-top: 30px;
        }
        .feature-list {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin: 20px 0;
        }
        .feature-card {
            background: #f7f7f7;
            padding: 20px;
            border-radius: 8px;
            border-left: 4px solid #667eea;
        }
        .feature-card h3 {
            margin-top: 0;
            color: #333;
        }
        .docs-section {
            background: #e8f4f8;
            padding: 20px;
            border-radius: 8px;
            margin: 20px 0;
        }
        .docs-link {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 12px 24px;
            border-radius: 6px;
            text-decoration: none;
            margin: 10px 10px 10px 0;
            transition: background 0.3s;
        }
        .docs-link:hover {
            background: #764ba2;
        }
        .status {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 4px;
            font-size: 14px;
            margin-left: 10px;
        }
        .status.done {
            background: #10b981;
            color: white;
        }
        .status.planned {
            background: #f59e0b;
            color: white;
        }
        code {
            background: #f4f4f4;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
        }
        .tech-stack {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin: 20px 0;
        }
        .tech-badge {
            background: #667eea;
            color: white;
            padding: 8px 16px;
            border-radius: 20px;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚀 Social Media Android App</h1>
        <p style="font-size: 18px; color: #666;">
            Ứng dụng mạng xã hội nhắn tin với tính năng đầy đủ, được xây dựng bằng Kotlin cho Android và Backend API bằng Ktor.
        </p>
        
        <div class="docs-section">
            <h2>📚 Documentation</h2>
            <a href="/README.md" class="docs-link">📖 README</a>
            <a href="/SETUP_GUIDE.md" class="docs-link">🛠️ Setup Guide</a>
            <a href="/WEBRTC_INTEGRATION.md" class="docs-link">📹 WebRTC Integration</a>
        </div>
        
        <h2>✨ Tính Năng</h2>
        <div class="feature-list">
            <div class="feature-card">
                <h3>🔐 Authentication</h3>
                <p>Đăng ký, đăng nhập với JWT</p>
                <span class="status done">✓ Hoàn thành</span>
            </div>
            <div class="feature-card">
                <h3>💬 Real-time Messaging</h3>
                <p>Chat trực tiếp qua WebSocket</p>
                <span class="status done">✓ Hoàn thành</span>
            </div>
            <div class="feature-card">
                <h3>📸 Media Sharing</h3>
                <p>Gửi ảnh, video</p>
                <span class="status done">✓ Hoàn thành</span>
            </div>
            <div class="feature-card">
                <h3>📞 Voice & Video Call</h3>
                <p>WebRTC integration</p>
                <span class="status planned">⚙️ Integration Guide</span>
            </div>
            <div class="feature-card">
                <h3>🤖 GitHub Actions</h3>
                <p>Auto build APK</p>
                <span class="status done">✓ Hoàn thành</span>
            </div>
            <div class="feature-card">
                <h3>🗄️ CockroachDB</h3>
                <p>PostgreSQL compatible</p>
                <span class="status done">✓ Hoàn thành</span>
            </div>
        </div>
        
        <h2>🛠️ Tech Stack</h2>
        <div class="tech-stack">
            <span class="tech-badge">Kotlin 1.9.22</span>
            <span class="tech-badge">Jetpack Compose</span>
            <span class="tech-badge">Ktor 2.3.7</span>
            <span class="tech-badge">CockroachDB</span>
            <span class="tech-badge">WebSocket</span>
            <span class="tech-badge">WebRTC</span>
            <span class="tech-badge">Material 3</span>
            <span class="tech-badge">Retrofit</span>
            <span class="tech-badge">OkHttp</span>
        </div>
        
        <h2>🚦 Quick Start</h2>
        <ol style="line-height: 2;">
            <li>Đọc <a href="/SETUP_GUIDE.md">SETUP_GUIDE.md</a> để thiết lập môi trường</li>
            <li>Tạo tài khoản CockroachDB tại <a href="https://cockroachlabs.com" target="_blank">cockroachlabs.com</a></li>
            <li>Clone repository và cấu hình biến môi trường</li>
            <li>Chạy backend: <code>./gradlew run</code></li>
            <li>Mở <code>android/</code> trong Android Studio và build app</li>
            <li>Push lên GitHub để tự động build APK</li>
        </ol>
        
        <h2>📁 Cấu Trúc Project</h2>
        <pre style="background: #f4f4f4; padding: 15px; border-radius: 8px; overflow-x: auto;">
.
├── android/                # Android app (Kotlin + Compose)
│   ├── app/
│   │   ├── src/main/java/com/socialmedia/app/
│   │   │   ├── data/      # Models, API, WebSocket
│   │   │   ├── ui/        # Screens, ViewModels, Theme
│   │   │   └── MainActivity.kt
│   │   └── build.gradle.kts
│   └── build.gradle.kts
│
├── src/                    # Backend Kotlin/Ktor
│   └── main/kotlin/com/socialmedia/
│       ├── models/        # Data models
│       ├── plugins/       # Ktor plugins
│       ├── routes/        # API routes
│       └── Application.kt
│
├── .github/workflows/     # GitHub Actions
│   └── android-build.yml  # Auto build APK
│
├── README.md              # Tổng quan dự án
├── SETUP_GUIDE.md         # Hướng dẫn cài đặt chi tiết
└── WEBRTC_INTEGRATION.md  # Hướng dẫn WebRTC
        </pre>
        
        <h2>🔗 Useful Links</h2>
        <ul>
            <li><a href="https://cockroachlabs.com" target="_blank">CockroachDB Console</a></li>
            <li><a href="https://ktor.io/docs/" target="_blank">Ktor Documentation</a></li>
            <li><a href="https://developer.android.com/jetpack/compose" target="_blank">Jetpack Compose</a></li>
            <li><a href="https://webrtc.org/" target="_blank">WebRTC Official</a></li>
        </ul>
        
        <div style="margin-top: 40px; padding-top: 20px; border-top: 2px solid #eee; color: #666; text-align: center;">
            <p>💡 <strong>Tip:</strong> Đọc kỹ SETUP_GUIDE.md để biết cách cài đặt và chạy dự án</p>
            <p>Made with ❤️ by Replit Agent</p>
        </div>
    </div>
</body>
</html>
            """
            self.wfile.write(html.encode())
        else:
            super().do_GET()

if __name__ == '__main__':
    with socketserver.TCPServer(("0.0.0.0", PORT), MyHTTPRequestHandler) as httpd:
        print(f"🚀 Documentation server running at http://0.0.0.0:{PORT}")
        print(f"📚 View project documentation at http://0.0.0.0:{PORT}")
        print(f"📖 README: http://0.0.0.0:{PORT}/README.md")
        print(f"🛠️ Setup Guide: http://0.0.0.0:{PORT}/SETUP_GUIDE.md")
        print(f"📹 WebRTC Guide: http://0.0.0.0:{PORT}/WEBRTC_INTEGRATION.md")
        httpd.serve_forever()
