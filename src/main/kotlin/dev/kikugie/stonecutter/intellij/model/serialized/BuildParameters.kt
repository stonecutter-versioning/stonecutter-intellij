package dev.kikugie.stonecutter.intellij.model.serialized

import dev.kikugie.semver.data.Version
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = ReplacementJsonSerializer::class)
sealed interface Replacement {
    val identifier: String?
}

@Serializable
data class StringReplacement(
    val sources: Set<String>,
    val target: String,
    override val identifier: String? = null
) : Replacement

@Serializable
data class RegexReplacement(
    val pattern: String,
    val target: String,
    override val identifier: String? = null
) : Replacement

@Serializable
data class BuildParameters(
    val constants: Map<String, Boolean> = emptyMap(),
    val dependencies: Map<String, Version> = emptyMap(),
    val swaps: Map<String, String> = emptyMap(),
    val replacements: List<Replacement> = emptyList(),
)

object ReplacementJsonSerializer : JsonContentPolymorphicSerializer<Replacement>(Replacement::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Replacement> = when (element) {
        is JsonObject -> matchObjectType(element)
        else -> throw SerializationException("Unable to deserialize ${element::class.qualifiedName}")
    }

    private fun matchObjectType(element: JsonObject): DeserializationStrategy<Replacement> {
        val type = element["type"]?.jsonPrimitive?.contentOrNull ?: throw SerializationException("Unable to determine replacement type")
        if ("StringReplacement" in type) return StringReplacement.serializer()
        if ("RegexReplacement" in type) return RegexReplacement.serializer()
        throw SerializationException("Unable to determine replacement type")
    }
}