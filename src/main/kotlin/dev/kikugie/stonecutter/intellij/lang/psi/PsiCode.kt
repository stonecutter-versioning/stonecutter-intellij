package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor

private val MARKERS = intArrayOf(StitcherLexer.COND_MARK, StitcherLexer.SWAP_MARK, StitcherLexer.REPL_MARK)

class PsiCode(node: ASTNode) : PsiStitcherNodeImpl(node) {
    val marker: PsiElement? get() = firstChild
    val definition: PsiDefinition? get() = lastChild as? PsiDefinition
    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitBlock(this)
}
