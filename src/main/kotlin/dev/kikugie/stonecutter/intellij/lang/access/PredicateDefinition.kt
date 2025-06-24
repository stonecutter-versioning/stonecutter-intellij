package dev.kikugie.stonecutter.intellij.lang.access

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.util.childrenSeqOfType

interface PredicateDefinition : PsiElement {
    val comparator: PsiElement? get() = childrenSeqOfType(StitcherTokenTypes.COMPARATOR).firstOrNull()
    val version: VersionDefinition get() = childrenSeqOfType<VersionDefinition>().first()
}