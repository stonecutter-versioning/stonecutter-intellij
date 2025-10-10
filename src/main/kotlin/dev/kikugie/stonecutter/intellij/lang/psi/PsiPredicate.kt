package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.VersionOperator
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

private val SEMVER_OPERATORS = intArrayOf(StitcherParser.COMMON_COMP, StitcherParser.SEMVER_COMP)

private fun PsiElement?.toVersionOperator(): VersionOperator = when(val text = this?.text) {
    null -> VersionOperator.IMPLICIT_EQUAL
    "!=" -> VersionOperator.NOT_EQUAL
    "=" -> VersionOperator.EQUAL
    ">=" -> VersionOperator.GREATER_EQUAL
    ">" -> VersionOperator.GREATER
    "<=" -> VersionOperator.LESS_EQUAL
    "<" -> VersionOperator.LESS
    "^" -> VersionOperator.SAME_MAJOR
    "~" -> VersionOperator.SAME_MINOR
    else -> error("Invalid operator $text")
}

sealed interface PsiPredicate : DefaultScopeNode {
    val operator: VersionOperator
    val version: PsiVersion
    val parsed: VersionPredicate get() = VersionPredicate(operator, version.parsed)

    fun <T> accept(visitor: StitcherVisitor<T>): T

    class Semantic(node: ASTNode) : ANTLRPsiNode(node), PsiPredicate {
        override val operator: VersionOperator
            get() = firstChild.takeIf { it.antlrType in SEMVER_OPERATORS }.toVersionOperator()

        override val version: PsiVersion
            get() = childrenSequence.firstIsInstance<PsiVersion.Semantic>()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitSemantic(this)
    }

    class String(node: ASTNode) : ANTLRPsiNode(node), PsiPredicate {
        override val operator: VersionOperator
            get() = firstChild.takeIf { it.antlrType == StitcherParser.COMMON_COMP }.toVersionOperator()

        override val version: PsiVersion
            get() = childrenSequence.firstIsInstance<PsiVersion.String>()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitString(this)
    }
}