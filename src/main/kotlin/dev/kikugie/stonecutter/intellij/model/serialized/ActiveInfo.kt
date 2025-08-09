package dev.kikugie.stonecutter.intellij.model.serialized

import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.Path

@Serializable @JvmInline
value class ActiveInfo private constructor(private val value: String?) {
    internal companion object {
        val UNKNOWN = ActiveInfo(null)
    }

    val isPath: Boolean get() = value?.startsWith("path@") == true
    val isIdentifier: Boolean get() = value?.startsWith("name@") == true
    val isUndefined: Boolean get() = value == null

    fun asPathOrNull(): Path? = value?.split { type, str ->
        if (type == "path") Path(str) else null
    }

    fun asIdentifierOrNull(): String? = value?.split { type, str ->
        str.takeIf { type == "name" }
    }

    private inline fun <T> String.split(action: (String, String) -> T): T {
        val index = indexOf('@')
        return action(take(index), substring(index + 1))
    }
}