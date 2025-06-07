package dev.kikugie.stonecutter.intellij.model

import kotlinx.serialization.Serializable

@Serializable
data class SCProcessProperties(
    val constants: Map<String, Boolean>,
    val dependencies: Map<String, Version>,
    val swaps: Set<String>,
    val replacements: Set<String>,
)
