package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

sealed interface PsiVersionPredicate {
    class Semantic(node: ASTNode) : ANTLRPsiNode(node), PsiVersionPredicate
    class String(node: ASTNode) : ANTLRPsiNode(node), PsiVersionPredicate
}