package com.sandymist.android.devicemonitor.power

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
sealed class PowerStatus {
    @Serializable
    data object Unknown: PowerStatus()

    @Serializable
    data class Available(
        val isPowerSaveMode: Boolean,
        val isDeviceIdleMode: Boolean,
    ): PowerStatus()
}

object PowerStatusSerializer : JsonTransformingSerializer<PowerStatus>(PowerStatus.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        // Remove the "type" field if present
        return if (element is JsonObject && "type" in element) {
            JsonObject(element.filterKeys { it != "type" })
        } else {
            element
        }
    }
}