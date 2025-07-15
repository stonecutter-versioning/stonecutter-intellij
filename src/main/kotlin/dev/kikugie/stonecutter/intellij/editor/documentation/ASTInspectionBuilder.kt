package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType

object ASTInspectionBuilder : DocumentationBuilder<PsiFile> {
    override fun applyTo(builder: StringBuilder, element: PsiFile) = with(builder) {
        append("<div class=\"definition\"><ul>")
        element.accept(Visitor(this))
        append("</ul></div>")
    }.let {  }

    private class Visitor(val builder: StringBuilder) : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement): Unit = when (element) {
            is PsiWhiteSpace -> {}
            is LeafPsiElement -> visitLeafElement(element)
            else -> visitCompositeElement(element)
        }.let {  }

        private fun visitLeafElement(element: PsiElement) = with(element) {
            builder.append("<li>$elementType@$textRange $text")
        }

        private fun visitCompositeElement(element: PsiElement) = with(element) {
            builder.append("<li>${this::class.simpleName}<ul>")
            acceptChildren(this@Visitor)
            builder.append("</ul>")
        }
    }
}