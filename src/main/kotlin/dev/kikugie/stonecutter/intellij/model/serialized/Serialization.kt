package dev.kikugie.stonecutter.intellij.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.reflect.KClass

internal object PathSerializer : KSerializer<Path> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("java.nio.file.Path", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Path): Unit = encoder.encodeString(value.invariantSeparatorsPathString)
    override fun deserialize(decoder: Decoder): Path = Path(decoder.decodeString())
}

internal object RegexPatternsSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Regex) =
        encoder.encodeString(value.pattern)

    override fun deserialize(decoder: Decoder): Regex =
        decoder.decodeString().toRegex()
}

internal object FlagContainerJsonSerializer : KSerializer<Map<String, Any>> {
    @Serializable @JvmInline
    private value class JsonFlagContainer(val map: Map<String, JsonPrimitive>)
    private val FLAG_MAPPINGS: Map<String, KClass<*>> = buildMap {
        this["implicit_receiver"] = String::class
    }

    override val descriptor: SerialDescriptor = JsonFlagContainer.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Map<String, Any>) = throw UnsupportedOperationException()
    override fun deserialize(decoder: Decoder): Map<String, Any> {
        val json = JsonFlagContainer.serializer().deserialize(decoder).map
        return buildMap {
            for ((key, value) in json) value.asAny(key)?.let { this[key] = it }
        }
    }

    private fun JsonPrimitive.asAny(key: String): Any? = when(FLAG_MAPPINGS.getOrDefault(key, Nothing::class)) {
        String::class -> content
        else -> null
    }
}