package dev.kikugie.stonecutter.intellij.lang.psi.visitor

import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap

open class StitcherPsiVisitor : PsiElementVisitor(), StitcherVisitor<Unit> {
    override fun visitDefinition(o: PsiDefinition): Unit = visitElement(o)
    override fun visitReplacement(o: PsiReplacement): Unit = visitElement(o)
    override fun visitSwap(o: PsiSwap): Unit = visitElement(o)
    override fun visitSwapArgs(o: PsiSwap.Args): Unit = visitElement(o)
    override fun visitCondition(o: PsiCondition): Unit = visitElement(o)
    override fun visitBinary(o: PsiExpression.Binary): Unit = visitElement(o)
    override fun visitUnary(o: PsiExpression.Unary): Unit = visitElement(o)
    override fun visitGroup(o: PsiExpression.Group): Unit = visitElement(o)
    override fun visitAssignment(o: PsiExpression.Assignment): Unit = visitElement(o)
    override fun visitConstant(o: PsiExpression.Constant): Unit = visitElement(o)
    override fun visitSemantic(o: PsiPredicate.Semantic): Unit = visitElement(o)
    override fun visitString(o: PsiPredicate.String): Unit = visitElement(o)
}