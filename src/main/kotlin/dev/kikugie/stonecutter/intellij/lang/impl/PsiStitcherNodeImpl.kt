package dev.kikugie.stonecutter.intellij.lang.impl

import com.intellij.lang.ASTNode
import dev.kikugie.stonecutter.intellij.lang.psi.PsiStitcherNode
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.intellij.adaptor.psi.ScopeNode

abstract class PsiStitcherNodeImpl(node: ASTNode) : ANTLRPsiNode(node), PsiStitcherNode {
    override fun getContext(): ScopeNode? = parent as? ScopeNode
}