package com.herrold.test.webrtc.webrtc.audio

typealias AudioDeviceChangeListener = (
    audioDevices: List<AudioDevice>,
    selectedAudioDevice: AudioDevice?,
) -> Unit
