package dev.kikugie.stonecutter.intellij.settings.naming

import java.util.function.Supplier

interface Supplied : Named {
    val namer: Supplier<String>
    val descriptor: Supplier<String>? get() = null

    override val display: String get() = namer.get()
    override val description: String? get() = descriptor?.get()
}