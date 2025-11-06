package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.elementOfAnyToken
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

private val LITERAL_TYPES = intArrayOf(StitcherLexer.IDENTIFIER, StitcherLexer.QUOTED)

interface PsiScopeMarker : PsiStitcherNode {
    class Closed(node: ASTNode) : ANTLRPsiNode(node), PsiScopeMarker {
        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitClosedScope(this)
    }

    class Word(node: ASTNode) : ANTLRPsiNode(node), PsiScopeMarker {
        val plus: PsiElement? get() = childrenSequence.elementOfAnyToken(StitcherLexer.PLUS)
        val literal: PsiElement? get() = lastChild?.takeIf { it.antlrType in LITERAL_TYPES }

        val isCapturing: Boolean get() = plus != null
        val expectedStr: String get() = literal?.unquote().orEmpty()

        override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitWordScope(this)
    }
}

private fun PsiElement.unquote() = when (antlrType) {
    StitcherLexer.IDENTIFIER -> text
    StitcherLexer.QUOTED -> text.removeSurrounding("'")
    else -> error("Invalid type $elementType")
}