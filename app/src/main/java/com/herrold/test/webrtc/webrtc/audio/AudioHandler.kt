package com.herrold.test.webrtc.webrtc.audio

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger

interface AudioHandler {
    /**
     * Called when a room is started.
     */
    fun start()

    /**
     * Called when a room is disconnected.
     */
    fun stop()
}

class AudioSwitchHandler constructor(private val context: Context) : AudioHandler {
    private val logger by taggedLogger(TAG)

    private val audioDeviceChangeListener: AudioDeviceChangeListener? = null
    private var onAudioFocusChangeListener: AudioManager.OnAudioFocusChangeListener? = null
    private var preferredDeviceList: List<Class<out AudioDevice>>? = null

    private var audioSwitch: AudioSwitch? = null

    private val handler = Handler(Looper.getMainLooper())

    override fun start() {
        logger.d { "[start] audioSwitch: $audioSwitch" }
        if (audioSwitch == null) {
            handler.removeCallbacksAndMessages(null)
            handler.post {
                val switch = AudioSwitch(
                    context = context,
                    audioFocusChangeListener = onAudioFocusChangeListener
                        ?: defaultOnAudioFocusChangeListener,
                    preferredDeviceList = preferredDeviceList ?: defaultPreferredDeviceList,
                )
                audioSwitch = switch
                switch.start(audioDeviceChangeListener ?: defaultAudioDeviceChangeListener)
                switch.activate()
            }
        }
    }

    override fun stop() {
        logger.d { "[stop] no args" }
        handler.removeCallbacksAndMessages(null)
        handler.post {
            audioSwitch?.stop()
            audioSwitch = null
        }
    }

    companion object {
        private const val TAG = "Call::AudioSwitchHandler"

        private val defaultOnAudioFocusChangeListener by lazy(LazyThreadSafetyMode.NONE) {
            DefaultOnAudioFocusChangeListener()
        }
        private val defaultAudioDeviceChangeListener by lazy(LazyThreadSafetyMode.NONE) {
            object : AudioDeviceChangeListener {
                override fun invoke(
                    audioDevices: List<AudioDevice>,
                    selectedAudioDevice: AudioDevice?,
                ) {
                    StreamLog.d(TAG) { "Audio devices: $audioDevices" }
                    StreamLog.d(TAG) { "Selected audio device: $selectedAudioDevice" }
                }
            }
        }

        private val defaultPreferredDeviceList by lazy(LazyThreadSafetyMode.NONE) {
            listOf(
                AudioDevice.BluetoothHeadset::class.java,
                AudioDevice.WiredHeadset::class.java,
                AudioDevice.Speaker::class.java,
                AudioDevice.Earpiece::class.java,
            )
        }

        private class DefaultOnAudioFocusChangeListener : AudioManager.OnAudioFocusChangeListener {
            override fun onAudioFocusChange(focusChange: Int) {
                val typeOfChange: String = when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> "AudioFocus: gained focus."
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> "AudioFocus: gained transient focus."
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE -> "AudioFocus: gained transient exclusive focus."
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> "AudioFocus: gained transient may duck focus."
                    AudioManager.AUDIOFOCUS_LOSS -> "AudioFocus: lost focus."
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> "AudioFocus: lost transient focus."
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> "AudioFocus: lost transient can duck focus."
                    else -> "AudioFocus: unknown focus change."
                }
                StreamLog.d(TAG) { "[onAudioFocusChange] focusChange: $typeOfChange" }
            }
        }
    }
}
