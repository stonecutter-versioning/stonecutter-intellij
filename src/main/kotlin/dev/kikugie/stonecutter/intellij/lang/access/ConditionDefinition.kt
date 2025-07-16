package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

/**
 * Provides easier access to components of [StitcherCondition][dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition].
 */
interface ConditionDefinition : PsiElement {
    val sugar: Sequence<PsiElement> get() = childrenSeqOfType(StitcherTokenTypes.SUGAR)
}