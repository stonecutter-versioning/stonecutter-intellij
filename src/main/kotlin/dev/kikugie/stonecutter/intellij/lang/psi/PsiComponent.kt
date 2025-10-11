package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.util.antlrType

sealed interface PsiComponent : PsiStitcherNode {
    val closer: PsiElement? get() = firstChild?.takeIf { it.antlrType == StitcherLexer.SCOPE_CLOSE }
    val opener: PsiElement? get() = lastChild?.takeIf { it.antlrType in SCOPE_OPENERS }
    val type: Type

    enum class Type {
        SCOPED_OPENER, LINE_OPENER, WORD_OPENER,
        SCOPED_EXTENSION, LINE_EXTENSION, WORD_EXTENSION,
        CLOSER, INDEPENDENT;

        val isScoped: Boolean get() = when(this) {
            SCOPED_OPENER, SCOPED_EXTENSION -> true
            else -> false
        }

        val isOpen: Boolean get() = when(this) {
            LINE_OPENER, WORD_OPENER, LINE_EXTENSION, WORD_EXTENSION -> true
            else -> false
        }

        val isExtension: Boolean get() = when(this) {
            SCOPED_EXTENSION, LINE_EXTENSION, WORD_EXTENSION, CLOSER -> true
            else -> false
        }

        val isEmpty: Boolean get() = when(this) {
            CLOSER, INDEPENDENT -> true
            else -> false
        }
    }

    companion object {
        val TYPE_KEY: Key<CachedValue<Type>> = Key("PsiComponent.type")
        val SCOPE_OPENERS = intArrayOf(StitcherLexer.SCOPE_OPEN, StitcherLexer.SCOPE_WORD)
    }
}