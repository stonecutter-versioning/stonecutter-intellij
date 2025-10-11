package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.util.CachedValue
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.*

private val PARSED_SEMVER_KEY: Key<CachedValue<SemanticVersion>> = Key("PsiVersion.Semantic.parsed")

sealed interface PsiVersion : PsiStitcherNode {
    val parsed: Version

    class String(node: ASTNode) : ASTWrapperPsiElement(node), PsiVersion {
        override val parsed: StringVersion get() = StringVersion(text)

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitStringVersion(this)
    }

    class Semantic(node: ASTNode) : ASTWrapperPsiElement(node), PsiVersion {
        override val parsed: SemanticVersion by cached(PARSED_SEMVER_KEY, ::buildSemver)
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitSemanticVersion(this)

        private fun buildSemver(): SemanticVersion {
            check(firstChild.antlrRule == StitcherParser.RULE_versionCore) { "No version core component" }
            val components = firstChild!!.childrenSequence
                .filter { it.antlrType == StitcherLexer.NUMERIC }
                .map { it.text.toInt() }
                .toList().toIntArray()
            val preRelease = childrenSequence.elementOfRule(StitcherParser.RULE_preRelease)
                ?.text.orEmpty()
            val buildMetadata = childrenSequence.elementOfRule(StitcherParser.RULE_buildMetadata)
                ?.text.orEmpty()
            return SemanticVersion(components, preRelease, buildMetadata)
        }
    }
}