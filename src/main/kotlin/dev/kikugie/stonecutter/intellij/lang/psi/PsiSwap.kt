package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.commons.collections.findIsInstance
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.psi.PsiComponent.Type
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.cached
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

private val SWAP_ARGS = intArrayOf(StitcherParser.IDENTIFIER, StitcherParser.QUOTED)

class PsiSwap(node: ASTNode) : ANTLRPsiNode(node), PsiComponent {
    val identifier: PsiElement? get() = firstChild?.takeIf { it.antlrType == StitcherParser.IDENTIFIER }
    val arguments: Args? get() = childrenSequence.findIsInstance<Args>()
    override val type: Type by cached(PsiComponent.TYPE_KEY, ::determineType)

    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitSwap(this)

    private fun determineType() = when {
        firstChild?.antlrType == StitcherParser.SCOPE_CLOSE -> Type.CLOSER
        lastChild?.antlrType == StitcherParser.SCOPE_OPEN -> Type.SCOPED_OPENER
        lastChild?.antlrType == StitcherParser.SCOPE_WORD -> Type.WORD_OPENER
        else -> Type.LINE_OPENER
    }

    class Args(node: ASTNode) : ANTLRPsiNode(node), PsiStitcherNode {
        val entries: Sequence<PsiElement> get() = childrenSequence.filter { it.antlrType in SWAP_ARGS }
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitSwapArgs(this)
    }
}