package dev.kikugie.stonecutter.intellij.model

import kotlinx.serialization.Serializable

@Serializable
data class SCProjectMetadata(
    val project: String,
    val version: String,
    val active: Boolean,
)
