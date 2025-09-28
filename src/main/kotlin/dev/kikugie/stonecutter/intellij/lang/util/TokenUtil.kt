package dev.kikugie.stonecutter.intellij.lang.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes

fun PsiElement.isSugar(): Boolean = when (elementType) {
    StitcherTokenTypes.SUGAR_IF,
    StitcherTokenTypes.SUGAR_ELIF,
    StitcherTokenTypes.SUGAR_ELSE -> true
    else -> false
}