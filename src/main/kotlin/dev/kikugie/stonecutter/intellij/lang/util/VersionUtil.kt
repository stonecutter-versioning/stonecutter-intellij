package dev.kikugie.stonecutter.intellij.lang.util

import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.access.VersionDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSemanticVersion
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherStringVersion
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

fun Version.Companion.convert(element: VersionDefinition): Version = when (element) {
    is StitcherStringVersion -> StringVersion.convert(element)
    is StitcherSemanticVersion -> SemanticVersion.convert(element)
    else -> error("Unsupported version type")
}

fun StringVersion.Companion.convert(element: StitcherStringVersion): StringVersion =
    StringVersion(element.text)

fun SemanticVersion.Companion.convert(element: StitcherSemanticVersion): SemanticVersion {
    val components = element.versionCore.childrenSeqOfType(StitcherTokenTypes.NUMERIC)
        .map { it.text.toInt() }.toList()
    val preRelease = element.preRelease?.text ?: ""
    val buildMetadata = element.buildMetadata?.text ?: ""
    return SemanticVersion(components.toIntArray(), preRelease, buildMetadata)
}