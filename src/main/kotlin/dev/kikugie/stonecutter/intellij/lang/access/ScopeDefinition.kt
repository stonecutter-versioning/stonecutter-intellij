package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwap
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

/**
 * Groups [StitcherCondition][dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition],
 * [StitcherSwap][dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwap]
 * and [StitcherReplacement][dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement].
 */
sealed interface ScopeDefinition : PsiElement {
    val closer: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.CLOSER).firstOrNull()
    val opener: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.OPENER).firstOrNull()
    val type: ScopeType get() = determineType()
}

private fun ScopeDefinition.determineType(): ScopeType = when (this) {
    is StitcherCondition -> determineConditionType()
    is StitcherSwap -> determineSwapType()
}

private fun StitcherSwap.determineSwapType(): ScopeType =
    match(closer != null, opener != null || swapKey != null).let {
        if (it != ScopeType.OPENER && it != ScopeType.CLOSER) ScopeType.INVALID else it
    }

private fun StitcherCondition.determineConditionType(): ScopeType =
    match(closer != null, opener != null || expression != null || sugar.firstOrNull() != null)

private fun match(hasCloser: Boolean, expectsScope: Boolean): ScopeType = when {
    hasCloser && expectsScope -> ScopeType.EXTENSION
    !hasCloser && expectsScope -> ScopeType.OPENER
    hasCloser -> ScopeType.CLOSER
    else -> ScopeType.INVALID
}
