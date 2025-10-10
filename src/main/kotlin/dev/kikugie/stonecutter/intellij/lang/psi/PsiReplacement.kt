package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

class PsiReplacement(node: ASTNode) : ANTLRPsiNode(node), PsiComponent, DefaultScopeNode {
    override val type: PsiComponent.Type = PsiComponent.Type.INDEPENDENT

    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitReplacement(this)
}