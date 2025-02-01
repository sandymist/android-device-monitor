package com.sandymist.android.devicemonitor.audio

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
sealed class AudioStatus {
    @Serializable
    data object Unknown: AudioStatus()

    @Serializable
    data class Available(
        val device: String,
    ): AudioStatus()
}

object AudioStatusSerializer : JsonTransformingSerializer<AudioStatus>(AudioStatus.serializer()) {
    override fun transformSerialize(element: JsonElement): JsonElement {
        // Remove the "type" field if present
        return if (element is JsonObject && "type" in element) {
            JsonObject(element.filterKeys { it != "type" })
        } else {
            element
        }
    }
}