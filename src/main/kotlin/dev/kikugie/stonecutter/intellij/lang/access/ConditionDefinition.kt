package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.util.isSugar
import dev.kikugie.stonecutter.intellij.util.childrenSequence

/**
 * Provides easier access to components of [StitcherCondition][dev.kikugie.stonecutter.intellij.lang.psi.StitcherCondition].
 */
interface ConditionDefinition : PsiElement {
    val sugar: Sequence<PsiElement> get() = childrenSequence.filter(PsiElement::isSugar)
}