package dev.kikugie.stonecutter.intellij.lang.psi.visitor

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.*

open class StitcherPsiVisitor : PsiElementVisitor(), StitcherVisitor<Unit> {
    override fun visitDefinition(o: PsiDefinition): Unit = Unit
    override fun visitReplacement(o: PsiReplacement): Unit = Unit
    override fun visitSwap(o: PsiSwap): Unit = Unit
    override fun visitSwapArgs(o: PsiSwap.Args): Unit = Unit
    override fun visitCondition(o: PsiCondition): Unit = Unit
    override fun visitBinary(o: PsiExpression.Binary): Unit = Unit
    override fun visitUnary(o: PsiExpression.Unary): Unit = Unit
    override fun visitGroup(o: PsiExpression.Group): Unit = Unit
    override fun visitAssignment(o: PsiExpression.Assignment): Unit = Unit
    override fun visitConstant(o: PsiExpression.Constant): Unit = Unit
    override fun visitSemanticPredicate(o: PsiPredicate.Semantic): Unit = Unit
    override fun visitStringPredicate(o: PsiPredicate.String): Unit = Unit
    override fun visitSemanticVersion(o: PsiVersion.Semantic): Unit = Unit
    override fun visitStringVersion(o: PsiVersion.String): Unit = Unit

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        if (element !is PsiStitcherNode) super.visitElement(element)
        else element.accept(this as StitcherVisitor<Unit>)
    }
}