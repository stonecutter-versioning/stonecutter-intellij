package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.commons.collections.firstIsInstance
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence

class PsiDefinition(node: ASTNode) : PsiStitcherNodeImpl(node) {
    val marker: PsiElement get() = firstChild!!
    val component: PsiComponent get() = childrenSequence.firstIsInstance<PsiComponent>()

    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitDefinition(this)
}