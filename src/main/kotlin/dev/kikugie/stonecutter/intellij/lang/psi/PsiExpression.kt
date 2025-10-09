package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

sealed interface PsiExpression : PsiElement {
    class Binary(node: ASTNode) : ANTLRPsiNode(node), PsiExpression
    class Unary(node: ASTNode) : ANTLRPsiNode(node), PsiExpression
    class Group(node: ASTNode) : ANTLRPsiNode(node), PsiExpression
    class Assignment(node: ASTNode) : ANTLRPsiNode(node), PsiExpression
    class Constant(node: ASTNode) : ANTLRPsiNode(node), PsiExpression
}