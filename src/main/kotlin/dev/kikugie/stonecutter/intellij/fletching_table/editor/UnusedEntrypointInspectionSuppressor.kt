package dev.kikugie.stonecutter.intellij.fletching_table.editor

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressManager
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiJavaFile
import dev.kikugie.stonecutter.intellij.fletching_table.model.FTEntrypointModel

class UnusedEntrypointInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean =
        if (toolId != "unused") false
        else when (element) {
            is PsiJavaFile -> suppressFileIssues(element)
            is PsiIdentifier -> suppressIdentifierIssue(element)
            else -> false
        }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<out SuppressQuickFix?> =
        SuppressManager.getInstance().getSuppressActions(element, toolId)

    private fun suppressFileIssues(file: PsiJavaFile): Boolean =
        file.classes.any(::suppressIssue)

    private fun suppressIdentifierIssue(identifier: PsiIdentifier): Boolean =
        (identifier.parent as? PsiClass)?.let(::suppressIssue) ?: false

    private fun suppressIssue(cls: PsiClass): Boolean = cls.annotations
        .any { it.qualifiedName == FTEntrypointModel.ENTRYPOINT_FQ }
}