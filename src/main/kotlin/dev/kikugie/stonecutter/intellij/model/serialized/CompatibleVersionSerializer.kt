package dev.kikugie.stonecutter.intellij.model.serialized

import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import kotlinx.serialization.json.*

object CompatibleVersionSerializer : JsonTransformingSerializer<Version>(Version.serializer()) {
    private val STR_FQN = checkNotNull(StringVersion::class.qualifiedName)
    private val SEM_FQN = checkNotNull(SemanticVersion::class.qualifiedName)

    override fun transformDeserialize(element: JsonElement): JsonElement = when(element) {
        is JsonPrimitive -> remapStringVersion(element)
        is JsonObject -> {
            val type = element["type"]?.jsonPrimitive?.contentOrNull ?: return element
            if (type == SEM_FQN) remapSemanticVersion(element) else element
        }
        else -> element
    }

    private fun remapStringVersion(elem: JsonElement) = buildJsonObject {
        put("type", STR_FQN)
        put("value", elem)
    }

    private fun remapSemanticVersion(obj: JsonObject) = buildJsonObject {
        for ((k, v) in obj) put(renameField(k), v)
    }

    private fun renameField(key: String) = when(key) {
        "preRelease" -> "pre_release"
        "buildMetadata" -> "build_metadata"
        else -> key
    }
}