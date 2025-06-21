package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition.DefinitionType
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwap
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

interface ScopeDefinition : PsiElement {
    enum class DefinitionType { OPENER, EXTENSION, CLOSER, INVALID }
    val closer: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.CLOSER).firstOrNull()
    val opener: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.OPENER).firstOrNull()
    val type: DefinitionType get() = determineType()
}

private fun ScopeDefinition.determineType(): DefinitionType = when (this) {
    is StitcherCondition -> determineConditionType()
    is StitcherSwap -> determineSwapType()
    else -> DefinitionType.INVALID
}

private fun StitcherSwap.determineSwapType(): DefinitionType =
    match(closer != null, opener != null || swapId != null).let {
        if (it != DefinitionType.OPENER && it != DefinitionType.CLOSER) DefinitionType.INVALID else it
    }

private fun StitcherCondition.determineConditionType(): DefinitionType =
    match(closer != null, opener != null || expression != null || sugar.firstOrNull() != null)

private fun match(hasCloser: Boolean, expectsScope: Boolean): DefinitionType = when {
    hasCloser && expectsScope -> DefinitionType.EXTENSION
    !hasCloser && expectsScope -> DefinitionType.OPENER
    hasCloser -> DefinitionType.CLOSER
    else -> DefinitionType.INVALID
}
