package com.herrold.test.webrtc.webrtc.peer

import android.content.Context
import android.os.Build
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule

class StreamPeerConnectionFactory(private val context: Context) {
    private val webRtcLogger by taggedLogger("Call:WebRTC")
    private val audioLogger by taggedLogger("Call:AudioTrack")

    val baseContext: EglBase.Context by lazy {
        EglBase.create().eglBaseContext
    }

    private val videoDecoderFactory by lazy {
        DefaultVideoDecoderFactory(baseContext)
    }

    val rtcConfig = PeerConnection.RTCConfiguration(
        arrayListOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        ),
    ).apply {
        sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
    }

    private val videoEncoderFactory by lazy {
        val hardwareEncoder = HardwareVideoEncoderFactory(baseContext, true, true)
        SimulcastVideoEncoderFactory(hardwareEncoder, SoftwareVideoEncoderFactory())
    }

    private val factory by lazy {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setInjectableLogger({ message, severity, labe ->
                    when (severity) {
                        Logging.Severity.LS_VERBOSE -> webRtcLogger.v { message }
                        Logging.Severity.LS_INFO -> webRtcLogger.i { message }
                        Logging.Severity.LS_WARNING -> webRtcLogger.w { message }
                        Logging.Severity.LS_ERROR -> webRtcLogger.e { message }
                        Logging.Severity.LS_NONE -> webRtcLogger.d { message }
                        else -> {}
                    }
                }, Logging.Severity.LS_VERBOSE)
                .createInitializationOptions(),
        )

        PeerConnectionFactory.builder()
            .setVideoDecoderFactory(videoDecoderFactory)
            .setVideoEncoderFactory(videoEncoderFactory)
            .setAudioDeviceModule(
                JavaAudioDeviceModule
                    .builder(context)
                    .setUseHardwareAcousticEchoCanceler(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    .setUseHardwareNoiseSuppressor(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    .setAudioRecordErrorCallback(object :
                        JavaAudioDeviceModule.AudioRecordErrorCallback {
                        override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                            audioLogger.w { "[onWebRtcAudioRecordInitError] $errorMessage" }
                        }

                        override fun onWebRtcAudioRecordStartError(
                            errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                            errorMessage: String?,
                        ) {
                            audioLogger.w { "[onWebRtcAudioRecordInitError] $errorMessage" }
                        }

                        override fun onWebRtcAudioRecordError(errorMessage: String?) {
                            audioLogger.w { "[onWebRtcAudioRecordError] $errorMessage" }
                        }
                    })
                    .setAudioTrackErrorCallback(object :
                        JavaAudioDeviceModule.AudioTrackErrorCallback {
                        override fun onWebRtcAudioTrackInitError(errorMessage: String?) {
                            audioLogger.w { "[onWebRtcAudioTrackInitError] $errorMessage" }
                        }

                        override fun onWebRtcAudioTrackStartError(
                            errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                            errorMessage: String?,
                        ) {
                            audioLogger.w { "[onWebRtcAudioTrackStartError] $errorMessage" }
                        }

                        override fun onWebRtcAudioTrackError(errorMessage: String?) {
                            audioLogger.w { "[onWebRtcAudioTrackError] $errorMessage" }
                        }
                    })
                    .setAudioRecordErrorCallback(object :
                        JavaAudioDeviceModule.AudioRecordErrorCallback {
                        override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                            audioLogger.w { "[onWebRtcAudioRecordInitError] $errorMessage" }
                        }

                        override fun onWebRtcAudioRecordStartError(
                            errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                            errorMessage: String?,
                        ) {
                            audioLogger.w { "[onWebRtcAudioRecordInitError] $errorMessage" }
                        }

                        override fun onWebRtcAudioRecordError(errorMessage: String?) {
                            audioLogger.w { "[onWebRtcAudioRecordError] $errorMessage" }
                        }
                    })
                    .setAudioTrackStateCallback(object :
                        JavaAudioDeviceModule.AudioTrackStateCallback {
                        override fun onWebRtcAudioTrackStart() {
                            audioLogger.i { "[onWebRtcAudioTrackStart]" }
                        }

                        override fun onWebRtcAudioTrackStop() {
                            audioLogger.i { "[onWebRtcAudioTrackStop]" }
                        }
                    })
                    .createAudioDeviceModule().also {
                        it.setMicrophoneMute(false)
                        it.setSpeakerMute(false)
                    },
            )
            .createPeerConnectionFactory()
    }

    fun makePeerConnection(
        coroutineScope: CoroutineScope,
        configuration: PeerConnection.RTCConfiguration,
        type: StreamPeerType,
        mediaConstraints: MediaConstraints,
        onStreamAdded: ((MediaStream) -> Unit)? = null,
        onNegotiationNeeded: ((StreamPeerConnection, StreamPeerType) -> Unit)? = null,
        onIceCandidate: ((IceCandidate, StreamPeerType) -> Unit)? = null,
        onVideoTrack: ((RtpTransceiver?) -> Unit)? = null,
    ): StreamPeerConnection {
        val peerConnection = StreamPeerConnection(
            coroutineScope,
            type,
            mediaConstraints,
            onStreamAdded,
            onNegotiationNeeded,
            onIceCandidate,
            onVideoTrack
        )
        val connection = makePeerConnectionInternal(configuration, peerConnection)

        return peerConnection.apply { initialize(connection) }
    }

    private fun makePeerConnectionInternal(
        configuration: PeerConnection.RTCConfiguration,
        observer: PeerConnection.Observer?,
    ): PeerConnection {
        return requireNotNull(
            factory.createPeerConnection(
                configuration,
                observer
            )
        )
    }

    fun makeVideoSource(isScreencast: Boolean): VideoSource =
        factory.createVideoSource(isScreencast)

    fun makeVideoTrack(
        source: VideoSource,
        trackId: String,
    ): VideoTrack = factory.createVideoTrack(trackId, source)

    fun makeAudioSource(constraints: MediaConstraints = MediaConstraints()): AudioSource =
        factory.createAudioSource(constraints)

    fun makeAudioTrack(
        source: AudioSource,
        trackId: String,
    ): AudioTrack = factory.createAudioTrack(trackId, source)
}
