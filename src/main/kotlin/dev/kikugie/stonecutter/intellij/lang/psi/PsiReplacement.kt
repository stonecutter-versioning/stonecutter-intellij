package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherVisitor

class PsiReplacement(node: ASTNode) : ASTWrapperPsiElement(node), PsiComponent {
    override val type: PsiComponent.Type = PsiComponent.Type.INDEPENDENT

    override fun <T> accept(visitor: StitcherVisitor<T>): T = visitor.visitReplacement(this)
}