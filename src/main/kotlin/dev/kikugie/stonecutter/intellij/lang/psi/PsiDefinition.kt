package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.commons.collections.findIsInstance
import dev.kikugie.commons.collections.firstIsInstance
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

class PsiDefinition(node: ASTNode) : ANTLRPsiNode(node) {
    val marker: PsiElement get() = firstChild!!
    val component: PsiComponent? get() = childrenSequence.findIsInstance<PsiComponent>()

    fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitDefinition(this)
}