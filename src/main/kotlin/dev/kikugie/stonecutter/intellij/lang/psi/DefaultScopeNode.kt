package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import org.antlr.intellij.adaptor.psi.ScopeNode

interface DefaultScopeNode : ScopeNode {
    override fun resolve(element: PsiNamedElement?): PsiElement? = null
}