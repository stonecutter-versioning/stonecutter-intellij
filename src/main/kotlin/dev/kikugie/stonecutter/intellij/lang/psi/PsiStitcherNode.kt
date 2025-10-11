package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor

interface PsiStitcherNode : PsiElement {
    fun <T> accept(visitor: StitcherVisitor<T>): T
}