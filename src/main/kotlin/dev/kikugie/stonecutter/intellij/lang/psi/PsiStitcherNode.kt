package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import org.antlr.intellij.adaptor.psi.ScopeNode

interface PsiStitcherNode : ScopeNode {
    override fun resolve(element: PsiNamedElement?): PsiElement? = null

    fun <T> accept(visitor: StitcherVisitor<T>): T
}