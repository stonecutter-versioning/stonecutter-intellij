package dev.kikugie.stonecutter.intellij.fletching_table.editor

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressManager
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiJavaFile
import dev.kikugie.stonecutter.intellij.fletching_table.FletchingTableAccessor
import dev.kikugie.stonecutter.intellij.fletching_table.FletchingTableService.Companion.fletchingTable

class UnusedMixinInspectionSuppressor : InspectionSuppressor {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (toolId != "UnusedMixin") return false
        val accessor = element.fletchingTable ?: return false
        return when(element) {
            is PsiJavaFile -> suppressFileIssues(element, accessor)
            is PsiIdentifier -> suppressIdentifierIssue(element, accessor)
            else -> false
        }
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<out SuppressQuickFix?> =
        SuppressManager.getInstance().getSuppressActions(element, toolId)

    private fun suppressFileIssues(file: PsiJavaFile, accessor: FletchingTableAccessor): Boolean =
        file.classes.any { suppressIssue(it, accessor)}

    private fun suppressIdentifierIssue(identifier: PsiIdentifier, accessor: FletchingTableAccessor): Boolean =
        (identifier.parent as? PsiClass)?.let { suppressIssue(it, accessor) } ?: false

    private fun suppressIssue(cls: PsiClass, accessor: FletchingTableAccessor): Boolean {
        val fqn = cls.qualifiedName ?: return false
        return accessor.mixins.any { it.implementation == fqn }
    }
}