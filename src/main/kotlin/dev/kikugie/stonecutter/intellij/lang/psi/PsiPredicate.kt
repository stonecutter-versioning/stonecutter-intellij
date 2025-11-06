package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.VersionOperator
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstance

private fun PsiElement?.toVersionOperator(): VersionOperator = when(this?.antlrType ?: -1) {
    StitcherLexer.COMP_NEQUAL -> VersionOperator.NOT_EQUAL
    StitcherLexer.COMP_EQUAL -> VersionOperator.EQUAL
    StitcherLexer.COMP_GMORE -> VersionOperator.GREATER_EQUAL
    StitcherLexer.COMP_MORE -> VersionOperator.GREATER
    StitcherLexer.COMP_GLESS -> VersionOperator.LESS_EQUAL
    StitcherLexer.COMP_LESS -> VersionOperator.LESS
    StitcherLexer.COMP_MAJOR -> VersionOperator.SAME_MAJOR
    StitcherLexer.COMP_MINOR -> VersionOperator.SAME_MINOR
    else -> VersionOperator.IMPLICIT_EQUAL
}

sealed interface PsiPredicate : PsiStitcherNode {
    val operator: VersionOperator
    val version: PsiVersion
    val parsed: VersionPredicate get() = VersionPredicate(operator, version.parsed)

    class String(node: ASTNode) : PsiStitcherNodeImpl(node), PsiPredicate {
        override val operator: VersionOperator
            get() = firstChild.toVersionOperator()

        override val version: PsiVersion
            get() = childrenSequence.firstIsInstance<PsiVersion.String>()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitStringPredicate(this)
    }

    class Semantic(node: ASTNode) : PsiStitcherNodeImpl(node), PsiPredicate {
        override val operator: VersionOperator
            get() = firstChild.toVersionOperator()

        override val version: PsiVersion
            get() = childrenSequence.firstIsInstance<PsiVersion.Semantic>()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitSemanticPredicate(this)
    }
}