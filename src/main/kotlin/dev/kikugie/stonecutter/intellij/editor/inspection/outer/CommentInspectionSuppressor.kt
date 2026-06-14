package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressManager
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiBlock
import dev.kikugie.stonecutter.intellij.lang.psi.findHostedBlock
import dev.kikugie.stonecutter.intellij.lang.psi.isCommentedOut

class CommentInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean = when {
        toolId != "CommentedOutCode" -> false
        element !is PsiComment -> false
        else -> inspectHostedCode(element)
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<out SuppressQuickFix?> {
        val actions = SuppressManager.getInstance().getSuppressActions(element, toolId)
        return actions
    }

    private fun inspectHostedCode(element: PsiComment): Boolean {
        val commentBlock = element.findHostedBlock() as? PsiBlock.Comment ?: return false
        val codeBlock = commentBlock.parent as? PsiBlock.Code ?: return false
        val isCommented = codeBlock.isCommentedOut
        return isCommented
    }
}