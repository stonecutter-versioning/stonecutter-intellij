package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType.SEM_BUILD
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType.SEM_PRE
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.cached
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.elementOfType

private val PARSED_SEMVER_KEY: Key<CachedValue<SemanticVersion?>> = Key("PsiVersion.Semantic.parsed")

sealed interface PsiVersion : PsiStitcherNode {
    val parsed: Version?

    class String(node: ASTNode) : PsiStitcherNodeImpl(node), PsiVersion {
        override val parsed: StringVersion get() = StringVersion(text)
    }

    class Semantic(node: ASTNode) : PsiStitcherNodeImpl(node), PsiVersion {
        override val parsed: SemanticVersion? by cached(PARSED_SEMVER_KEY, ::buildSemver)

        private fun buildSemver(): SemanticVersion? {
            val components = (firstChild ?: return null).childrenSequence
                .filter { it.antlrType == StitcherLexer.NUMERIC }
                .map { it.text.toInt() }
                .toList().toIntArray()
            val preRelease = childrenSequence.elementOfType(SEM_PRE.asIElementType())
                ?.text.orEmpty()
            val buildMetadata = childrenSequence.elementOfType(SEM_BUILD.asIElementType())
                ?.text.orEmpty()
            return SemanticVersion(components, preRelease, buildMetadata)
        }
    }
}