package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.psi.PsiElement

interface DocumentationBuilder<T : PsiElement> {
    fun applyTo(builder: StringBuilder, element: T)
}