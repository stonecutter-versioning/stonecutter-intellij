package dev.kikugie.stonecutter.intellij.lang.psi.visitor

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiStitcherNode
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.psi.PsiVersion


open class StitcherPsiVisitor : PsiElementVisitor(), StitcherVisitor<Unit> {
    override fun visitDefinition(o: PsiDefinition): Unit = checked(o) { component.accept(it) }
    override fun visitReplacement(o: PsiReplacement): Unit = visitElement(o)
    override fun visitSwap(o: PsiSwap): Unit = checked(o) { arguments?.accept(it) }
    override fun visitSwapArgs(o: PsiSwap.Args): Unit = visitElement(o)
    override fun visitCondition(o: PsiCondition): Unit = checked(o) { expression?.accept(it) }
    override fun visitBinary(o: PsiExpression.Binary): Unit = checked(o) { left.accept(it); right.accept(it) }
    override fun visitUnary(o: PsiExpression.Unary): Unit = checked(o) { target.accept(it) }
    override fun visitGroup(o: PsiExpression.Group): Unit = checked(o) { body.accept(it) }
    override fun visitAssignment(o: PsiExpression.Assignment): Unit = checked(o) { for (p in predicates) p.accept(it) }
    override fun visitConstant(o: PsiExpression.Constant): Unit = visitElement(o)
    override fun visitSemanticPredicate(o: PsiPredicate.Semantic): Unit = checked(o) { version.accept(it) }
    override fun visitStringPredicate(o: PsiPredicate.String): Unit = checked(o) { version.accept(it) }
    override fun visitSemanticVersion(o: PsiVersion.Semantic) = visitElement(o)
    override fun visitStringVersion(o: PsiVersion.String) = visitElement(o)

    private inline fun <T : PsiStitcherNode> checked(it: T, action: T.(StitcherVisitor<Unit>) -> Unit) {
        ProgressIndicatorProvider.checkCanceled()
        action(it, this)
    }
}