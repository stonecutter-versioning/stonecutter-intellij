package dev.kikugie.stonecutter.intellij.lang.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

enum class StitcherCompositeType {
    COND, SWAP, REPL,
    COND_OPEN, COND_EXT, COND_CLOSE,
    SWAP_OPEN, SWAP_CLOSE, SWAP_LOCAL, SWAP_EXPR,
    REPL_TOGGLE, REPL_OPEN, REPL_CLOSE, TOGGLE_ENTRY, LOCAL_ENTRY,
    GROUP, UNARY, BINARY, CONSTANT, ASSIGNMENT,
    CLOSED_SCOPE, LOOKUP_SCOPE, NAMED_SCOPE,
    SEM_PRED, STR_PRED,
    SEM_VER, STR_VER,
    SEM_CORE, SEM_PRE, SEM_BUILD
    ;

    fun asIElementType(): CompositeIElementType = TYPES[ordinal]

    companion object {
        private val TYPES: List<CompositeIElementType> = entries.map(::CompositeIElementType)
    }
}

/**
 * The token type used for ANTLR rules.
 *
 * This type is used instead of [RuleIElementType][org.antlr.intellij.adaptor.lexer.RuleIElementType]
 * because the parser relies on the named rule branches, which don't have unique IDs.
 */
class CompositeIElementType internal constructor(val value: StitcherCompositeType) : IElementType(value.name, StitcherLang)

val PsiElement?.compositeType: StitcherCompositeType?
    inline get() = elementType?.compositeType

val IElementType?.compositeType: StitcherCompositeType?
    get() = this?.takeAsOrNull<CompositeIElementType>()?.value

