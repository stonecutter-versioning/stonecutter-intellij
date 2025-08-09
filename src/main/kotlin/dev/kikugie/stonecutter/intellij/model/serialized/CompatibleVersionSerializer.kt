package dev.kikugie.stonecutter.intellij.model.serialized

import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.Version
import kotlinx.serialization.json.*

object CompatibleVersionSerializer : JsonTransformingSerializer<Version>(Version.serializer()) {
    private val SEM_FQN = checkNotNull(SemanticVersion::class.qualifiedName)

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element !is JsonObject) return element

        val type = element["type"]?.jsonPrimitive?.contentOrNull ?: return element
        return if (type == SEM_FQN) renameSemanticVersion(element) else element
    }

    private fun renameSemanticVersion(obj: JsonObject) = buildJsonObject {
        for ((k, v) in obj) put(renameField(k), v)
    }

    private fun renameField(key: String) = when(key) {
        "preRelease" -> "pre_release"
        "buildMetadata" -> "build_metadata"
        else -> key
    }
}