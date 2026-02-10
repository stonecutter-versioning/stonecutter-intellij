package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.commons.collections.findIsInstance
import dev.kikugie.semver.data.VersionOperator
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence

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

class PsiPredicate(node: ASTNode) : PsiStitcherNodeImpl(node) {
    val operator: VersionOperator get() = firstChild.toVersionOperator()
    val version: PsiVersion? get() = childrenSequence.findIsInstance<PsiVersion>()
    val parsed: VersionPredicate? get() = version?.let { VersionPredicate(operator, it.parsed) }
}