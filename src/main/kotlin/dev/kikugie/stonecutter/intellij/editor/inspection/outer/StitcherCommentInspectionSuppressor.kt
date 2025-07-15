package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.access.ScopeType
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.util.filterNotWhitespace
import dev.kikugie.stonecutter.intellij.util.prevSiblings

class StitcherCommentInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean = toolId == "CommentedOutCode"
        && element is PsiComment
        && element.prevSiblings.filterNotWhitespace().firstOrNull()
        .let {
            val type = (it as? PsiComment)?.commentDefinition?.element?.type
            type != null && type != ScopeType.CLOSER
        }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<out SuppressQuickFix?> =
        emptyArray()
}