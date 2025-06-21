package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

interface ScopeDefinition : PsiElement {
    val closer: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.CLOSER).firstOrNull()
    val opener: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.OPENER).firstOrNull()
}