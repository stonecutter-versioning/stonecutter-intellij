package dev.kikugie.stonecutter.intellij.model

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.util.*

@Serializable(with = VersionJsonSerializer::class)
sealed interface Version : Comparable<Version>

@Serializable @JvmInline
value class StringVersion(val value: String) : Version {
    override fun toString(): String = value
    override fun compareTo(other: Version): Int =
        value.compareTo(other.toString())
}

@Serializable
data class SemanticVersion(val components: IntArray, val preRelease: String = "", val buildMetadata: String = "") : Version {
    private val friendlyName by lazy {
        buildString {
            append(components.joinToString("."))
            if (preRelease.isNotEmpty())
                append("-$preRelease")
            if (buildMetadata.isNotEmpty())
                append("+$buildMetadata")
        }
    }

    override fun toString(): String = friendlyName
    override fun compareTo(other: Version): Int = when(other) {
        is StringVersion -> friendlyName.compareTo(other.value)
        is SemanticVersion -> compareComponents(other)
            .let { if (it != 0) it else compareToPreModifier(other) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SemanticVersion) return false

        if (!components.contentEquals(other.components)) return false
        if (preRelease != other.preRelease) return false
        if (buildMetadata != other.buildMetadata) return false

        return true
    }

    override fun hashCode(): Int {
        var result = components.contentHashCode()
        result = 31 * result + preRelease.hashCode()
        result = 31 * result + buildMetadata.hashCode()
        return result
    }

    private fun compareComponents(other: SemanticVersion): Int {
        for (i in 0 until maxOf(components.size, other.components.size)) {
            val first = components.getOrElse(i) { 0 }
            val second = other.components.getOrElse(i) { 0 }
            first.compareTo(second).let { if (it != 0) return it }
        }
        return 0
    }

    private fun compareToPreModifier(other: SemanticVersion): Int {
        if (preRelease.isEmpty() && preRelease.isEmpty()) return 0
        if (preRelease.isEmpty() && other.preRelease.isNotEmpty()) return 1
        if (buildMetadata.isNotEmpty() && other.buildMetadata.isEmpty()) return -1
        if (buildMetadata.isEmpty() && other.buildMetadata.isNotEmpty()) return 1

        val myTokenizer = StringTokenizer(preRelease, ".")
        val otherTokenizer = StringTokenizer(other.preRelease, ".")

        while (myTokenizer.hasMoreElements() || otherTokenizer.hasMoreElements()) {
            if (!myTokenizer.hasMoreElements()) return -1
            if (!otherTokenizer.hasMoreElements()) return 1

            val myPart = myTokenizer.nextToken()
            val otherPart = otherTokenizer.nextToken()

            val myPartInt = myPart.toIntOrNull()
            val otherPartInt = otherPart.toIntOrNull()

            if (myPartInt != null && otherPartInt != null) {
                val compare = myPartInt.compareTo(otherPartInt)
                if (compare != 0) return compare
            }
            if (myPartInt == null && otherPartInt != null)
                return 1
            if (myPartInt != null && otherPartInt == null)
                return -1
            val compare = myPart.compareTo(otherPart)
            if (compare != 0) return compare
        }
        return 0
    }
}

private object VersionJsonSerializer : JsonContentPolymorphicSerializer<Version>(Version::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Version> = when (element) {
        is JsonObject -> SemanticVersion.serializer()
        is JsonPrimitive -> StringVersion.serializer()
        else -> throw SerializationException("Unable to deserialize ${element::class.qualifiedName}")
    }
}