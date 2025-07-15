package dev.kikugie.stonecutter.intellij.settings.naming

interface Named {
    val display: String
    val description: String? get() = null
}

