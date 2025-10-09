package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

sealed interface PsiPredicate {
    fun <T> accept(visitor: StitcherVisitor<T>): T

    class Semantic(node: ASTNode) : ANTLRPsiNode(node), PsiPredicate {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitSemantic(this)
    }

    class String(node: ASTNode) : ANTLRPsiNode(node), PsiPredicate {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitString(this)
    }
}