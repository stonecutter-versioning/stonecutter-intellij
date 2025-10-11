package dev.kikugie.stonecutter.intellij.lang.psi.visitor

import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import dev.kikugie.stonecutter.intellij.lang.psi.PsiVersion

interface StitcherVisitor<T> {
    fun visitDefinition(o: PsiDefinition): T
    fun visitReplacement(o: PsiReplacement): T
    fun visitSwap(o: PsiSwap): T
    fun visitSwapArgs(o: PsiSwap.Args): T
    fun visitCondition(o: PsiCondition): T
    fun visitBinary(o: PsiExpression.Binary): T
    fun visitUnary(o: PsiExpression.Unary): T
    fun visitGroup(o: PsiExpression.Group): T
    fun visitAssignment(o: PsiExpression.Assignment): T
    fun visitConstant(o: PsiExpression.Constant): T
    fun visitSemanticPredicate(o: PsiPredicate.Semantic): T
    fun visitStringPredicate(o: PsiPredicate.String): T
    fun visitSemanticVersion(o: PsiVersion.Semantic): T
    fun visitStringVersion(o: PsiVersion.String): T
}