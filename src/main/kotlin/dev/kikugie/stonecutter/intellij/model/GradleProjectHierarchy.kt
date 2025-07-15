package dev.kikugie.stonecutter.intellij.model

import kotlinx.serialization.Serializable

@JvmInline @Serializable
value class GradleProjectHierarchy(val path: String) {
    init {
        require(path.isNotBlank()) { "Path cannot be blank" }
        require(path.startsWith(':')) { "Path must start with ':'" }
    }

    fun orEmpty() = if (path == ":") "" else path

    fun trim() = path.removePrefix(":")

    operator fun plus(child: String): GradleProjectHierarchy = GradleProjectHierarchy("${orEmpty()}:$child")

    override fun toString(): String = path
}