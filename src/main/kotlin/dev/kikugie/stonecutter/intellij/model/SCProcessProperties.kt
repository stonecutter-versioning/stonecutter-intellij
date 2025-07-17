package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.intellij.model.serialized.Replacement
import kotlinx.serialization.Serializable

@Serializable
data class SCProcessProperties(
    val constants: Map<String, Boolean>,
    val dependencies: Map<String, Version>,
    val swaps: Map<String, String>,
    val replacements: Replacements,
) {
    @JvmInline
    @Serializable
    @Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
    value class Replacements(private val replacements: List<Replacement>) : Collection<Replacement> by replacements {
        operator fun get(id: String) = replacements.find { it.identifier == id }
    }
}
