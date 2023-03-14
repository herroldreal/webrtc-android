package com.herrold.test.webrtc.webrtc.sessions

import com.herrold.test.webrtc.webrtc.SignalingClient
import com.herrold.test.webrtc.webrtc.peer.StreamPeerConnectionFactory
import kotlinx.coroutines.flow.SharedFlow
import org.webrtc.VideoTrack

interface WebRtcSessionManager {

    val signalingClient: SignalingClient

    val peerConnectionFactory: StreamPeerConnectionFactory

    val localVideoTrackFlow: SharedFlow<VideoTrack>

    val remoteVideoTrackFlow: SharedFlow<VideoTrack>

    fun onSessionScreenReady()

    fun flipCamera()

    fun enableMicrophone(enabled: Boolean)

    fun enableCamera(enabled: Boolean)

    fun disconnect()
}