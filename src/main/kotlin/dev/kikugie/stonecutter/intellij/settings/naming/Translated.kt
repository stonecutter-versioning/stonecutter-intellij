package dev.kikugie.stonecutter.intellij.settings.naming

import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import org.jetbrains.annotations.PropertyKey

interface Translated : Named {
    val namer: @PropertyKey(resourceBundle = BUNDLE) String
    val descriptor: @PropertyKey(resourceBundle = BUNDLE) String? get() = null

    override val display: String get() = namer.let { StonecutterBundle.message(it) }
    override val description: String? get() = descriptor?.let { StonecutterBundle.message(it) }
}