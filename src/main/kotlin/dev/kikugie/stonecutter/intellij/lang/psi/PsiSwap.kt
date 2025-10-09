package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

class PsiSwap(node: ASTNode) : ANTLRPsiNode(node), PsiComponent {
    class Args(node: ASTNode) : ANTLRPsiNode(node)
}