package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import dev.kikugie.commons.collections.findIsInstance
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.util.childrenSequence

sealed interface ExpressionDefinition : PsiElement {
    interface Member : PsiElement

    @JvmInline
    value class Binary internal constructor(private val definition: ExpressionDefinition) : Member, PsiElement by definition {
        val operator: PsiElement get() = childrenSequence.findIsInstance<LeafPsiElement>()!!
        val left: Member get() = checkNotNull(firstChild.asMember()) { "Expected $firstChild to be an expression member" }
        val right: Member get() = checkNotNull(lastChild.asMember()) { "Expected $lastChild to be an expression member" }
    }

    @JvmInline
    value class Unary internal constructor(private val definition: ExpressionDefinition) : Member, PsiElement by definition {
        val operator: PsiElement get() = firstChild
        val element: Member get() = checkNotNull(lastChild.asMember()) { "Expected $lastChild to be an expression member" }
    }

    @JvmInline
    value class Group internal constructor(private val definition: ExpressionDefinition) : Member, PsiElement by definition {
        val body: Member get() = checkNotNull(findBody()?.asMember()) { "Expected body to be an expression member" }
        private fun findBody() = childrenSequence.find { it is ExpressionDefinition || it is Member }
    }

    fun asMember() : Member? = when {
        this is Member -> this
        firstChild.elementType == StitcherTokenTypes.LEFT_BRACE -> Group(this)
        firstChild.elementType == StitcherTokenTypes.UNARY -> Unary(this)
        childrenSequence.any { it.elementType == StitcherTokenTypes.BINARY } -> Binary(this)
        else -> null
    }
}

private fun PsiElement.asMember() = if (this is ExpressionDefinition) asMember() else null