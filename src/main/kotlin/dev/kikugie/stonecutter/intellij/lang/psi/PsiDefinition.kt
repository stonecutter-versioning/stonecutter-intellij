package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.commons.collections.firstIsInstance
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.intellij.adaptor.psi.ScopeNode

class PsiDefinition(node: ASTNode) : ANTLRPsiNode(node), PsiStitcherNode {
    val marker: PsiElement get() = firstChild!!
    val component: PsiComponent get() = childrenSequence.firstIsInstance<PsiComponent>()

    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitDefinition(this)
    override fun getContext(): ScopeNode? = null
}