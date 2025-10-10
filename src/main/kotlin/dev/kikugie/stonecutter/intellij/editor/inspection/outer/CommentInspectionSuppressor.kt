package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.lang.util.filterNotWhitespace
import dev.kikugie.stonecutter.intellij.lang.util.prevSiblings

class CommentInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean = toolId == "CommentedOutCode"
        && element is PsiComment
        && element.prevSiblings.filterNotWhitespace().firstOrNull()
        .let {
            val type = (it as? PsiComment)?.commentDefinition?.element?.component?.type
            type != null && type.isScoped
        }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<out SuppressQuickFix?> =
        emptyArray()
}