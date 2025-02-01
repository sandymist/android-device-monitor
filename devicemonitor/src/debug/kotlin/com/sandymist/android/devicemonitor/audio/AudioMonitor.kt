package com.sandymist.android.devicemonitor.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class AudioMonitor private constructor(context: Context) {
    private val _audioStatus = MutableStateFlow<AudioStatus>(AudioStatus.Unknown)
    val audioStatus = _audioStatus.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun startTracking() {
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        logCurrentAudioOutput()  // Log the initial output device
    }

    fun stopTracking() {
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) {
            logCurrentAudioOutput()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) {
            logCurrentAudioOutput()
        }
    }

    private fun logCurrentAudioOutput() {
        val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val activeDevice = devices.firstOrNull { it.isActiveAudioDevice() }

        val activeDeviceDescription = activeDevice?.let {
            when (it.type) {
                AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth"
                AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
                AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Speaker"
                AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
                else -> "Unknown"
            }
        } ?: "Unknown 111"
        scope.launch {
            _audioStatus.emit(AudioStatus.Available(activeDeviceDescription))
        }
    }

    // Extension function to check if a device is an active audio device
    private fun AudioDeviceInfo.isActiveAudioDevice(): Boolean {
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).contains(this)
    }

    companion object {
        @Volatile
        private var INSTANCE: AudioMonitor? = null

        fun getInstance(context: Context): AudioMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioMonitor(context).also { INSTANCE = it }
            }
        }
    }
}
