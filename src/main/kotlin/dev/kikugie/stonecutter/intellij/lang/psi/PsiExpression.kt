package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.commons.collections.firstIsInstance
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.elementOfAnyToken
import dev.kikugie.stonecutter.intellij.lang.util.elementOfToken
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

sealed interface PsiExpression : PsiStitcherNode {
    class Binary(node: ASTNode) : ANTLRPsiNode(node), PsiExpression {
        val left: PsiExpression get() = firstChild as PsiExpression
        val right: PsiExpression get() = lastChild as PsiExpression
        val operator: PsiElement get() = checkNotNull(childrenSequence.elementOfAnyToken(StitcherLexer.OP_AND, StitcherLexer.OP_NOT))

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitBinary(this)
    }

    class Unary(node: ASTNode) : ASTWrapperPsiElement(node), PsiExpression {
        val target: PsiExpression get() = lastChild as PsiExpression
        val operator: PsiElement get() = firstChild!!

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitUnary(this)
    }

    class Group(node: ASTNode) : ASTWrapperPsiElement(node), PsiExpression {
        val body: PsiExpression get() = childrenSequence.firstIsInstance<PsiExpression>()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitGroup(this)
    }

    class Assignment(node: ASTNode) : ASTWrapperPsiElement(node), PsiExpression {
        val target: PsiElement? get() = firstChild.takeIf { it.antlrType == StitcherLexer.IDENTIFIER }
        val operator: PsiElement? get() = childrenSequence.elementOfToken(StitcherLexer.OP_ASSIGN)
        val predicates: Sequence<PsiPredicate> get() = childrenSequence.filterIsInstance<PsiPredicate>()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitAssignment(this)
    }

    class Constant(node: ASTNode) : ASTWrapperPsiElement(node), PsiExpression {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitConstant(this)
    }
}