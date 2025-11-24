# Hướng Dẫn Tích Hợp WebRTC (Voice & Video Call)

## 📋 Tổng Quan

WebRTC (Web Real-Time Communication) cho phép gọi thoại và video trực tiếp giữa các thiết bị. Document này hướng dẫn tích hợp WebRTC vào ứng dụng.

## 🏗️ Kiến Trúc WebRTC

```
User A                    Signaling Server                User B
  |                              |                           |
  |------ Create Offer --------->|                           |
  |                              |------- Send Offer ------->|
  |                              |<------ Create Answer -----|
  |<----- Receive Answer --------|                           |
  |                              |                           |
  |<--------------- ICE Candidates Exchange ---------------->|
  |                              |                           |
  |<=============== Direct P2P Connection ==================>|
```

## 📦 Dependencies

Đã được thêm vào `android/app/build.gradle.kts`:

```kotlin
implementation("io.getstream:stream-webrtc-android:1.1.3")
```

## 🔧 Implementation Steps

### Bước 1: Tạo WebRTC Manager

Tạo file `android/app/src/main/java/com/socialmedia/app/webrtc/WebRTCManager.kt`:

```kotlin
package com.socialmedia.app.webrtc

import android.content.Context
import io.getstream.webrtc.android.ktx.*
import org.webrtc.*

class WebRTCManager(private val context: Context) {
    
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private val eglContext = EglBase.create().eglBaseContext
    
    init {
        initializePeerConnectionFactory()
    }
    
    private fun initializePeerConnectionFactory() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(eglContext, true, true)
            )
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglContext)
            )
            .createPeerConnectionFactory()
    }
    
    fun createPeerConnection(
        iceServers: List<PeerConnection.IceServer>,
        observer: PeerConnection.Observer
    ): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        
        peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig, observer)
        return peerConnection
    }
    
    fun createVideoTrack(): VideoTrack? {
        val videoCapturer = createCameraCapturer() ?: return null
        val videoSource = peerConnectionFactory?.createVideoSource(videoCapturer.isScreencast)
        
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglContext)
        videoCapturer.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
        videoCapturer.startCapture(1280, 720, 30)
        
        return peerConnectionFactory?.createVideoTrack("video_track", videoSource)
    }
    
    fun createAudioTrack(): AudioTrack? {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        return peerConnectionFactory?.createAudioTrack("audio_track", audioSource)
    }
    
    private fun createCameraCapturer(): VideoCapturer? {
        val enumerator = Camera2Enumerator(context)
        
        // Thử camera trước
        enumerator.deviceNames.find { enumerator.isFrontFacing(it) }?.let {
            return enumerator.createCapturer(it, null)
        }
        
        // Nếu không có, dùng camera sau
        enumerator.deviceNames.find { enumerator.isBackFacing(it) }?.let {
            return enumerator.createCapturer(it, null)
        }
        
        return null
    }
    
    fun createOffer(
        peerConnection: PeerConnection,
        onSuccess: (SessionDescription) -> Unit,
        onError: (String) -> Unit
    ) {
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            onSuccess(it)
                        }
                        override fun onSetFailure(error: String?) {
                            onError(error ?: "Set local description failed")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }
            
            override fun onCreateFailure(error: String?) {
                onError(error ?: "Create offer failed")
            }
            
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }
    
    fun createAnswer(
        peerConnection: PeerConnection,
        onSuccess: (SessionDescription) -> Unit,
        onError: (String) -> Unit
    ) {
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                sdp?.let {
                    peerConnection.setLocalDescription(object : SdpObserver {
                        override fun onSetSuccess() {
                            onSuccess(it)
                        }
                        override fun onSetFailure(error: String?) {
                            onError(error ?: "Set local description failed")
                        }
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onCreateFailure(p0: String?) {}
                    }, it)
                }
            }
            
            override fun onCreateFailure(error: String?) {
                onError(error ?: "Create answer failed")
            }
            
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }
    
    fun handleRemoteOffer(
        peerConnection: PeerConnection,
        sdp: SessionDescription,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        peerConnection.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                onSuccess()
            }
            
            override fun onSetFailure(error: String?) {
                onError(error ?: "Set remote description failed")
            }
            
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }
    
    fun addIceCandidate(peerConnection: PeerConnection, candidate: IceCandidate) {
        peerConnection.addIceCandidate(candidate)
    }
    
    fun getIceServers(): List<PeerConnection.IceServer> {
        return listOf(
            // Google STUN server
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer(),
            
            // Bạn có thể thêm TURN server nếu cần
            // PeerConnection.IceServer.builder("turn:your-turn-server.com:3478")
            //     .setUsername("username")
            //     .setPassword("password")
            //     .createIceServer()
        )
    }
    
    fun dispose() {
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
    }
}
```

### Bước 2: Tạo Video Call Screen

Tạo file `android/app/src/main/java/com/socialmedia/app/ui/screens/call/VideoCallScreen.kt`:

```kotlin
package com.socialmedia.app.ui.screens.call

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.webrtc.SurfaceViewRenderer
import com.socialmedia.app.webrtc.WebRTCManager

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VideoCallScreen(
    userId: Int,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val webRTCManager = remember { WebRTCManager(context) }
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    var isMuted by remember { mutableStateOf(false) }
    var isVideoOff by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            webRTCManager.dispose()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Remote video (fullscreen)
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    init(webRTCManager.eglContext, null)
                    setMirror(false)
                    setEnableHardwareScaler(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Local video (picture-in-picture)
        AndroidView(
            factory = { ctx ->
                SurfaceViewRenderer(ctx).apply {
                    init(webRTCManager.eglContext, null)
                    setMirror(true)
                    setEnableHardwareScaler(true)
                    setZOrderMediaOverlay(true)
                }
            },
            modifier = Modifier
                .width(120.dp)
                .height(160.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )
        
        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Mute button
            FloatingActionButton(
                onClick = { isMuted = !isMuted },
                containerColor = if (isMuted) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute"
                )
            }
            
            // End call button
            FloatingActionButton(
                onClick = onEndCall,
                containerColor = MaterialTheme.colorScheme.error
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = "End Call")
            }
            
            // Video toggle button
            FloatingActionButton(
                onClick = { isVideoOff = !isVideoOff },
                containerColor = if (isVideoOff) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    if (isVideoOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = "Video"
                )
            }
        }
    }
}
```

### Bước 3: Update Backend để hỗ trợ WebRTC Signaling

Backend đã có WebSocket support và WebRTCSignal model. Bạn chỉ cần đảm bảo client gửi đúng format:

```kotlin
// Trong ChatViewModel
fun initiateVideoCall(recipientId: Int) {
    viewModelScope.launch {
        val offer = webRTCManager.createOffer()
        
        webSocketManager.sendMessage(
            WebRTCSignal(
                type = "offer",
                senderId = currentUserId,
                recipientId = recipientId,
                signal = offer.description
            )
        )
    }
}
```

### Bước 4: Add Navigation cho Video Call

Trong `NavGraph.kt`:

```kotlin
composable(Screen.VideoCall.route) { backStackEntry ->
    val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
    VideoCallScreen(
        userId = userId,
        onEndCall = {
            navController.popBackStack()
        }
    )
}
```

## 🔐 Permissions

Đã được thêm vào `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

## 🧪 Testing

### Test Local (2 Emulators)

1. Mở 2 Android emulators
2. Run app trên cả 2
3. Đăng nhập 2 users khác nhau
4. Initiate video call từ một user
5. Accept từ user kia

### Test với TURN Server

Nếu cần test qua Internet (không cùng mạng LAN):

1. Deploy TURN server (coturn)
2. Update `getIceServers()` với TURN credentials
3. Test với 2 devices khác mạng

## 📚 Resources

- [WebRTC Official](https://webrtc.org/)
- [Stream WebRTC Android](https://github.com/GetStream/webrtc-android)
- [PeerConnection API](https://webrtc.github.io/webrtc-org/native-code/android/)

## 🐛 Troubleshooting

### Video không hiển thị
- Kiểm tra permissions đã được granted
- Kiểm tra SurfaceViewRenderer đã init đúng
- Check Logcat cho lỗi WebRTC

### Không kết nối được
- Đảm bảo signaling server (WebSocket) hoạt động
- Kiểm tra ICE candidates được exchange
- Có thể cần TURN server nếu NAT traversal thất bại

### Audio echo
- Sử dụng earphones
- Enable echo cancellation trong AudioManager

Chúc bạn thành công với tính năng voice/video call! 🎥📞
