package com.herrold.test.webrtc.webrtc

import io.getstream.log.taggedLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.Request
import okhttp3.OkHttpClient
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import com.herrold.test.webrtc.BuildConfig

class SignalingClient {
    private val logger by taggedLogger("Call:SignalingClient")
    private val signalingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val httpClient = OkHttpClient()
    private val request = Request.Builder().url(BuildConfig.SIGNALING_SERVER_IP_ADDRESS).build()

    val webSocket = OkHttpClient().newWebSocket(request, object: WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            logger.d { "received message: $text" }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            logger.d { "closed: $code $reason" }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            logger.d { "failure: $t" }
        }

        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            logger.d { "opened" }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            logger.d { "closing: $code $reason" }
        }
    })

    private val ws = httpClient.newWebSocket(request, SignalingWebSocketListener())

    // session flow to send information about the session state to the subscribers
    private val _sessionStateFlow = MutableStateFlow(WebRTCSessionState.Offline)
    val sessionStateFlow: StateFlow<WebRTCSessionState> = _sessionStateFlow

    // signaling commands to send commands to value pairs to the subscribers
    private val _signalingCommandFlow = MutableSharedFlow<Pair<SignalingCommand, String>>()
    val signalingCommandFlow: SharedFlow<Pair<SignalingCommand, String>> = _signalingCommandFlow

    fun sendCommand(signalingCommand: SignalingCommand, message: String) {
        logger.d { "[sendCommand] $signalingCommand $message" }
        ws.send("$signalingCommand $message")
    }

    fun dispose() {
        _sessionStateFlow.value = WebRTCSessionState.Offline
        signalingScope.cancel()
        ws.cancel()
    }

    private inner class SignalingWebSocketListener : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            when {
                text.startsWith(SignalingCommand.STATE.toString(), true) ->
                    handleStateMessage(text)
                text.startsWith(SignalingCommand.OFFER.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.OFFER, text)
                text.startsWith(SignalingCommand.ANSWER.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.ANSWER, text)
                text.startsWith(SignalingCommand.ICE.toString(), true) ->
                    handleSignalingCommand(SignalingCommand.ICE, text)
            }
        }
    }

    private fun handleStateMessage(message: String) {
        val state = getSeparatedMessage(message)
        _sessionStateFlow.value = WebRTCSessionState.valueOf(state)
    }

    private fun handleSignalingCommand(command: SignalingCommand, text: String) {
        val value = getSeparatedMessage(text)
        logger.d { "received signaling: $command $value" }
        signalingScope.launch {
            _signalingCommandFlow.emit(command to value)
        }
    }

    private fun getSeparatedMessage(text: String) = text.substringAfter(' ')
}

enum class WebRTCSessionState {
    Active,
    Creating,
    Ready,
    Impossible,
    Offline,
}

enum class SignalingCommand {
    STATE,
    OFFER,
    ANSWER,
    ICE,
    CANDIDATE,
    BYE,
}
