package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapId

enum class ReferenceType {
    CONSTANT, DEPENDENCY, REPLACEMENT, SWAP;
    val id = name.lowercase()

    companion object {
        val PsiElement.referenceType
            get() = when (this) {
                is StitcherConstant -> CONSTANT
                is StitcherDependency -> DEPENDENCY
                is StitcherReplacement -> REPLACEMENT
                is StitcherSwapId -> SWAP
                else -> null
            }

        val ReferenceType.textAttribute
            get() = when (this) {
                CONSTANT -> AttributeKeys.CONSTANT
                DEPENDENCY -> AttributeKeys.DEPENDENCY
                REPLACEMENT -> AttributeKeys.REPLACEMENT
                SWAP -> AttributeKeys.SWAP
            }
    }
}

/**
 * Determines the enclosing type of [ScopeDefinition].
 * - [ScopeType.OPENER]: `? ... ({|>>)`
 * - [ScopeType.EXTENSION]: `?} ... ({|>>)`
 * - [ScopeType.CLOSER]: `?}`
 * - [ScopeType.INVALID]: the comment syntax is invalid.
 */
enum class ScopeType {
    OPENER, EXTENSION, CLOSER, INVALID;
}

/**
 * Determines the region [ScopeDefinition] affects.
 * - [OpenerType.LINE]: the next line will be affected.
 * - [OpenerType.WORD]: the next sequence of non-whitespace characters will be affected.
 * - [OpenerType.OPEN]: everything until a [ScopeType.EXTENSION] or [ScopeType.CLOSER] comment will be affected.
 */
enum class OpenerType {
    LINE, WORD, OPEN;
}