package dev.kikugie.stonecutter.intellij.lang.psi.visitor

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCode
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiScope
import dev.kikugie.stonecutter.intellij.lang.psi.PsiStitcherNode
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.psi.PsiVersion
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

interface StitcherVisitor<T> : PsiDefinition.Visitor<T>, PsiExpression.Visitor<T> {
    fun visitBlock(code: PsiCode): T = nope()
    fun visitGeneric(node: PsiStitcherNode): T = nope()

    override fun visitSwapLocal(swap: PsiSwap.Local): T = nope()
    override fun visitSwapOpener(swap: PsiSwap.Opener): T = nope()
    override fun visitSwapCloser(swap: PsiSwap.Closer): T = nope()
    override fun visitConditionOpener(condition: PsiCondition.Opener): T = nope()
    override fun visitConditionExtension(condition: PsiCondition.Extension): T = nope()
    override fun visitConditionCloser(condition: PsiCondition.Closer): T = nope()
    override fun visitReplacementToggle(replacement: PsiReplacement.Toggle): T = nope()
    override fun visitReplacementLocal(replacement: PsiReplacement.Local): T = nope()
    override fun visitReplacementCloser(replacement: PsiReplacement.Closer): T = nope()

    override fun visitGroup(group: PsiExpression.Group): T = nope()
    override fun visitUnary(unary: PsiExpression.Unary): T = nope()
    override fun visitBinary(binary: PsiExpression.Binary): T = nope()
    override fun visitConstant(constant: PsiExpression.Constant): T = nope()
    override fun visitAssignment(assignment: PsiExpression.Assignment): T = nope()
}

@OptIn(ExperimentalContracts::class)
open class StitcherPsiVisitor : PsiElementVisitor(), StitcherVisitor<Unit> {
    override fun visitBlock(code: PsiCode): Unit = Unit
    override fun visitGeneric(node: PsiStitcherNode): Unit = Unit

    override fun visitSwapLocal(swap: PsiSwap.Local): Unit = Unit
    override fun visitSwapOpener(swap: PsiSwap.Opener): Unit = Unit
    override fun visitSwapCloser(swap: PsiSwap.Closer): Unit = Unit
    override fun visitConditionOpener(condition: PsiCondition.Opener): Unit = Unit
    override fun visitConditionExtension(condition: PsiCondition.Extension): Unit = Unit
    override fun visitConditionCloser(condition: PsiCondition.Closer): Unit = Unit
    override fun visitReplacementToggle(replacement: PsiReplacement.Toggle): Unit = Unit
    override fun visitReplacementLocal(replacement: PsiReplacement.Local): Unit = Unit
    override fun visitReplacementCloser(replacement: PsiReplacement.Closer): Unit = Unit

    override fun visitGroup(group: PsiExpression.Group): Unit = Unit
    override fun visitUnary(unary: PsiExpression.Unary): Unit = Unit
    override fun visitBinary(binary: PsiExpression.Binary): Unit = Unit
    override fun visitConstant(constant: PsiExpression.Constant): Unit = Unit
    override fun visitAssignment(assignment: PsiExpression.Assignment): Unit = Unit

    override fun visitElement(element: PsiElement): Unit = checked {
        if (element !is PsiStitcherNode) super.visitElement(element)
        else element.accept(this as StitcherVisitor<Unit>)
    }

    inline fun <T> checked(action: () -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        ProgressIndicatorProvider.checkCanceled()
        return action()
    }

    inline fun <I, T> checked(body: I, action: I.() -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        ProgressIndicatorProvider.checkCanceled()
        return action(body)
    }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun nope(): Nothing = throw UnsupportedOperationException()
