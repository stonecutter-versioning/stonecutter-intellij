package dev.kikugie.stonecutter.intellij.lang.util

import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.semver.data.VersionOperator
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.access.PredicateDefinition
import dev.kikugie.stonecutter.intellij.lang.access.VersionDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSemanticVersion
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherStringVersion
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

fun SemanticVersion(vararg components: Int, preRelease: String = "", buildMetadata: String = ""): SemanticVersion =
    SemanticVersion(components, preRelease, buildMetadata)

fun Version.Companion.convert(element: VersionDefinition): Version = when (element) {
    is StitcherStringVersion -> StringVersion.convert(element)
    is StitcherSemanticVersion -> SemanticVersion.convert(element)
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

fun VersionPredicate.Companion.convert(element: PredicateDefinition): VersionPredicate {
    val operator = element.comparator.toVersionOperator()
    val version = Version.convert(element.version)
    return VersionPredicate(operator, version)
}

private fun PsiElement?.toVersionOperator() = when(val value = this?.text.orEmpty()) {
    "" -> VersionOperator.IMPLICIT_EQUAL
    "=" -> VersionOperator.EQUAL
    "<" -> VersionOperator.LESS
    ">" -> VersionOperator.GREATER
    "<=" -> VersionOperator.LESS_EQUAL
    ">=" -> VersionOperator.GREATER_EQUAL
    "~" -> VersionOperator.SAME_MINOR
    "^" -> VersionOperator.SAME_MAJOR
    else -> throw IllegalArgumentException("Invalid operator '$value'")
}