package dev.kikugie.stonecutter.intellij.lang.psi.visitor

import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.psi.PsiVersion

interface StitcherSelectiveVisitor<T> : StitcherVisitor<T> {
    override fun visitDefinition(o: PsiDefinition): T = throw UnsupportedOperationException()
    override fun visitReplacement(o: PsiReplacement): T = throw UnsupportedOperationException()
    override fun visitSwap(o: PsiSwap): T = throw UnsupportedOperationException()
    override fun visitSwapArgs(o: PsiSwap.Args): T = throw UnsupportedOperationException()
    override fun visitCondition(o: PsiCondition): T = throw UnsupportedOperationException()
    override fun visitBinary(o: PsiExpression.Binary): T = throw UnsupportedOperationException()
    override fun visitUnary(o: PsiExpression.Unary): T = throw UnsupportedOperationException()
    override fun visitGroup(o: PsiExpression.Group): T = throw UnsupportedOperationException()
    override fun visitAssignment(o: PsiExpression.Assignment): T = throw UnsupportedOperationException()
    override fun visitConstant(o: PsiExpression.Constant): T = throw UnsupportedOperationException()
    override fun visitSemanticPredicate(o: PsiPredicate.Semantic): T = throw UnsupportedOperationException()
    override fun visitStringPredicate(o: PsiPredicate.String): T = throw UnsupportedOperationException()
    override fun visitSemanticVersion(o: PsiVersion.Semantic): T = throw UnsupportedOperationException()
    override fun visitStringVersion(o: PsiVersion.String): T = throw UnsupportedOperationException()
}