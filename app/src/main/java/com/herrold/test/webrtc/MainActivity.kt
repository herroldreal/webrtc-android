package com.herrold.test.webrtc

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.herrold.test.webrtc.ui.screens.stage.StageScreen
import com.herrold.test.webrtc.ui.screens.video.VideoCallScreen
import com.herrold.test.webrtc.ui.theme.WebrtcSampleComposeTheme
import com.herrold.test.webrtc.webrtc.SignalingClient
import com.herrold.test.webrtc.webrtc.peer.StreamPeerConnectionFactory
import com.herrold.test.webrtc.webrtc.sessions.LocalWebRtcSessionManager
import com.herrold.test.webrtc.webrtc.sessions.WebRtcSessionManager
import com.herrold.test.webrtc.webrtc.sessions.WebRtcSessionManagerImpl

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)

        val sessionManager: WebRtcSessionManager = WebRtcSessionManagerImpl(
            context = this,
            signalingClient = SignalingClient(),
            peerConnectionFactory = StreamPeerConnectionFactory(this)
        )

        setContent {
            WebrtcSampleComposeTheme {
                CompositionLocalProvider(LocalWebRtcSessionManager provides sessionManager) {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        var onCallScreen by remember { mutableStateOf(false) }
                        val state by sessionManager.signalingClient.sessionStateFlow.collectAsState()

                        if (!onCallScreen) {
                            StageScreen(state = state) { onCallScreen = true }
                        } else {
                            VideoCallScreen()
                        }
                    }
                }
            }
        }
    }
}