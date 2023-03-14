package com.herrold.test.webrtc.webrtc.audio

sealed class AudioDevice {
    abstract val name: String

    data class BluetoothHeadset internal constructor(override val name: String = "Bluetooth") : AudioDevice()
    data class WiredHeadset internal constructor(override val name: String = "Wired") : AudioDevice()
    data class Speaker internal constructor(override val name: String = "Speaker") : AudioDevice()
    data class Earpiece internal constructor(override val name: String = "Earpiece") : AudioDevice()
}
