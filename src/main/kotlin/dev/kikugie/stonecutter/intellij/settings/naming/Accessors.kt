@file:Suppress("NOTHING_TO_INLINE")

package dev.kikugie.stonecutter.intellij.settings.naming

internal inline fun <T> T.display(fallback: (T) -> String): String = if (this is Named) display else fallback(this)
internal inline fun <T> T.description(): String? = if (this is Named) description else null