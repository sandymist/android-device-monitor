package com.sandymist.android.devicemonitor

import android.content.Context
import com.sandymist.android.devicemonitor.audio.AudioMonitor
import com.sandymist.android.devicemonitor.network.INetworkStatus
import com.sandymist.android.devicemonitor.network.NetworkMonitor
import com.sandymist.android.devicemonitor.network.NetworkStatus
import com.sandymist.android.devicemonitor.power.PowerMonitor
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

class DeviceMonitor(context: Context) {
    private var networkMonitor = NetworkMonitor.getInstance(context)
    private var powerMonitor = PowerMonitor.getInstance(context)
    private var audioMonitor = AudioMonitor.getInstance(context)

    private val networkStatusFlow = networkMonitor.networkStatus
    private val powerStatusFlow = powerMonitor.powerStatus
    private val audioMonitorFlow = audioMonitor.audioStatus
    private val pretty = Json {
        prettyPrint = false
        serializersModule = SerializersModule {
            polymorphic(INetworkStatus::class) {
                subclass(NetworkStatus.Connected::class, NetworkStatus.Connected.serializer())
                subclass(NetworkStatus.Disconnected::class, NetworkStatus.Disconnected.serializer())
            }
        }
    }

    init {
        audioMonitor.startTracking()
    }

    @Suppress("unused")
    val deviceMonitorFlow =
        combine(networkStatusFlow, powerStatusFlow, audioMonitorFlow) { networkStatus, powerStatus, audioStatus ->
            val deviceStatus = DeviceStatus(networkStatus, powerStatus, audioStatus)
            val jsonString = pretty.encodeToString(deviceStatus)
            jsonString
        }
}
