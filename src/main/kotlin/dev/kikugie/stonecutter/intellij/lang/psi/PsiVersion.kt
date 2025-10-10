package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.elementType
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.util.antlrRule
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.cached
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.elementOfRule
import org.antlr.intellij.adaptor.psi.ANTLRPsiLeafNode
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

private val PARSED_SEMVER_KEY: Key<CachedValue<SemanticVersion>> = Key("PsiVersion.Semantic.parsed")

sealed interface PsiVersion : PsiElement {
    val parsed: Version

    class String(element: PsiElement) : ANTLRPsiLeafNode(element.elementType, element.text), PsiVersion {
        override val parsed: StringVersion get() = StringVersion(text)
    }

    class Semantic(node: ASTNode) : ANTLRPsiNode(node), PsiVersion {
        override val parsed: SemanticVersion by cached(PARSED_SEMVER_KEY, ::buildSemver)

        private fun buildSemver(): SemanticVersion {
            check(firstChild.antlrRule == StitcherParser.RULE_versionCore) { "No version core component" }
            val components = firstChild.childrenSequence
                .filter { it.antlrType == StitcherParser.NUMERIC }
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