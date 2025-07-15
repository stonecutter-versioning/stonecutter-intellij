package dev.kikugie.stonecutter.intellij.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import dev.kikugie.stonecutter.intellij.editor.documentation.ASTInspectionBuilder
import dev.kikugie.stonecutter.intellij.editor.documentation.ConstantDocBuilder
import dev.kikugie.stonecutter.intellij.editor.documentation.DependencyDocBuilder
import dev.kikugie.stonecutter.intellij.editor.documentation.StitcherDocumentationTarget
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant

class StitcherDocumentationTargetProvider : DocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> =
        if (file.language != StitcherLang) emptyList()
        else file.findElementAt(offset)?.let(::documentationTargets).orEmpty()

    private fun documentationTargets(element: PsiElement): List<DocumentationTarget> = buildList {
        findDocumentationTarget(element)?.let(::add)
        if (ApplicationManager.getApplication().isInternal)
            StitcherDocumentationTarget(element.containingFile, ASTInspectionBuilder).let(::add)
    }

    private fun findDocumentationTarget(element: PsiElement): DocumentationTarget? = element.parents(true)
        .find { it is StitcherConstant || it is StitcherAssignment }?.let {
            if (it is StitcherConstant) StitcherDocumentationTarget(it, ConstantDocBuilder)
            else StitcherDocumentationTarget(it as StitcherAssignment, DependencyDocBuilder)
        }
}
