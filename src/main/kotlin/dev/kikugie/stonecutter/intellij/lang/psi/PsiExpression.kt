package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

sealed interface PsiExpression : PsiElement {
    fun <T> accept(visitor: StitcherVisitor<T>): T

    class Binary(node: ASTNode) : ANTLRPsiNode(node), PsiExpression {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitBinary(this)
    }

    class Unary(node: ASTNode) : ANTLRPsiNode(node), PsiExpression {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitUnary(this)
    }

    class Group(node: ASTNode) : ANTLRPsiNode(node), PsiExpression {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitGroup(this)
    }

    class Assignment(node: ASTNode) : ANTLRPsiNode(node), PsiExpression {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitAssignment(this)
    }

    class Constant(node: ASTNode) : ANTLRPsiNode(node), PsiExpression {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitConstant(this)
    }
}