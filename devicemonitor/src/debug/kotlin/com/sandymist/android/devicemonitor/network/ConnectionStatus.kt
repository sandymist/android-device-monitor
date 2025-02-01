package com.sandymist.android.devicemonitor.network

import kotlinx.serialization.Serializable

@Serializable
data class ConnectionStatus(
    val usingWiFi: Boolean,
    val usingCellular: Boolean,
    val usingVPN: Boolean,
    val isValidated: Boolean,
    val isBehindCaptivePortal: Boolean,
    val isNotMetered: Boolean,
) {
    override fun toString(): String {
        return "WiFI $usingWiFi, Cellular $usingCellular, VPN $usingVPN, Validated $isValidated, Behind captive $isBehindCaptivePortal, Unmetered $isNotMetered"
    }
}