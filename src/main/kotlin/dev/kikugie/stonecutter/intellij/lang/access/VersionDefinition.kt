package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSemanticVersion
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherStringVersion
import dev.kikugie.stonecutter.intellij.model.SemanticVersion
import dev.kikugie.stonecutter.intellij.model.StringVersion
import dev.kikugie.stonecutter.intellij.model.Version
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

interface VersionDefinition : PsiElement {
    fun asDefinedVersion(): Version = when (this) {
        is StitcherSemanticVersion -> convertSemanticVersion()
        is StitcherStringVersion -> convertStringVersion()
        else -> error("Unsupported version type")
    }
}

private fun StitcherSemanticVersion.convertSemanticVersion(): SemanticVersion {
    val components = versionCore.childrenSeqOfType(StitcherTokenTypes.NUMERIC)
        .map { it.text.toInt() }.toList()
    val preRelease = preRelease?.text ?: ""
    val buildMetadata = buildMetadata?.text ?: ""
    return SemanticVersion(components.toIntArray(), preRelease, buildMetadata)
}

private fun StitcherStringVersion.convertStringVersion(): StringVersion =
    StringVersion(text)