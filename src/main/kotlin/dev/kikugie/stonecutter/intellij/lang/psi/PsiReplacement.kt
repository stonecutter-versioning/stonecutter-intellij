package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

class PsiReplacement(node: ASTNode) : ANTLRPsiNode(node), PsiComponent {
}