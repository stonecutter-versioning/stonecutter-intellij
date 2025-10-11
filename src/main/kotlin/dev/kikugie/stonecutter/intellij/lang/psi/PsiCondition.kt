package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.psi.PsiComponent.Companion.SCOPE_OPENERS
import dev.kikugie.stonecutter.intellij.lang.psi.PsiComponent.Type
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.cached
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

class PsiCondition(node: ASTNode) : ANTLRPsiNode(node), PsiComponent {
    override val type: Type by cached(PsiComponent.TYPE_KEY, ::determineType)

    private fun determineType(): Type {
        val closer = firstChild?.takeIf { it.antlrType == StitcherLexer.SCOPE_CLOSE }
        val opener = lastChild?.takeIf { it.antlrType in SCOPE_OPENERS }
        return matchType(closer != null, opener?.antlrType ?: -1)
    }

    private fun matchType(closer: Boolean, opener: Int): Type = when(opener) {
        StitcherLexer.SCOPE_OPEN -> if (closer) Type.SCOPED_EXTENSION else Type.SCOPED_OPENER
        StitcherLexer.SCOPE_WORD -> if (closer) Type.WORD_EXTENSION else Type.WORD_OPENER
        else -> if (closer) Type.LINE_EXTENSION else Type.LINE_OPENER
    }

    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitCondition(this)
}