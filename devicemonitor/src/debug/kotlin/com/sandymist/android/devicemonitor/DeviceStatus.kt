package com.sandymist.android.devicemonitor

import com.sandymist.android.devicemonitor.audio.AudioStatus
import com.sandymist.android.devicemonitor.audio.AudioStatusSerializer
import com.sandymist.android.devicemonitor.network.INetworkStatus
import com.sandymist.android.devicemonitor.network.INetworkStatusSerializer
import com.sandymist.android.devicemonitor.power.PowerStatus
import com.sandymist.android.devicemonitor.power.PowerStatusSerializer
import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatus(
    @Serializable(with = INetworkStatusSerializer::class)
    val networkStatus: INetworkStatus,
    @Serializable(with = PowerStatusSerializer::class)
    val powerStatus: PowerStatus,
    @Serializable(with = AudioStatusSerializer::class)
    val audioStatus: AudioStatus,
)
