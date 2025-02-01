package com.sandymist.android.devicemonitor.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
sealed interface INetworkStatus {
    val since: Long
    val isInAirplaneMode: Boolean
    fun statusName(): String
    fun details(): String
}

@Serializable
sealed class NetworkStatus: INetworkStatus {
    @Serializable
    data class Unknown(
        override val since: Long,
        override val isInAirplaneMode: Boolean
    ): NetworkStatus()

    @Serializable
    data class Connected(
        val availableConnectionStatus: ConnectionStatus?,
        val activeConnectionStatus: ConnectionStatus?,
        override val since: Long,
        override val isInAirplaneMode: Boolean
    ) : NetworkStatus() {
        override fun details(): String {
            return toString()
        }
    }

    @Serializable
    data class Disconnected(
        val activeConnectionStatus: ConnectionStatus?,
        override val since: Long,
        override val isInAirplaneMode: Boolean
    ): NetworkStatus() {
        override fun details(): String {
            return toString()
        }
    }

    override fun statusName(): String {
        return this::class.simpleName ?: super.toString()
    }

    override fun details(): String {
        return statusName()
    }
}

object INetworkStatusSerializer : JsonTransformingSerializer<INetworkStatus>(INetworkStatus.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        // Remove the "type" field if present
        return if (element is JsonObject && "type" in element) {
            JsonObject(element.filterKeys { it != "type" })
        } else {
            element
        }
    }
}
