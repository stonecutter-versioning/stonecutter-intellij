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
import dev.kikugie.stonecutter.intellij.editor.documentation.ReplacementLocalDocBuild
import dev.kikugie.stonecutter.intellij.editor.documentation.ReplacementToggleDocBuilder
import dev.kikugie.stonecutter.intellij.editor.documentation.StitcherDocumentationTarget
import dev.kikugie.stonecutter.intellij.editor.documentation.SwapIdDocBuilder
import dev.kikugie.stonecutter.intellij.editor.documentation.SwapLocalDocBuilder
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap

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
        .firstNotNullOfOrNull(::matchDocumentationTarget)

    private fun matchDocumentationTarget(element: PsiElement): StitcherDocumentationTarget<out PsiElement>? = when (element) {
        is PsiSwap.Local -> StitcherDocumentationTarget(element, SwapLocalDocBuilder)
        is PsiSwap.Opener -> StitcherDocumentationTarget(element, SwapIdDocBuilder)
        is PsiReplacement.Local -> StitcherDocumentationTarget(element, ReplacementLocalDocBuild)
        is PsiReplacement.Entry -> StitcherDocumentationTarget(element, ReplacementToggleDocBuilder)
        is PsiExpression.Constant -> StitcherDocumentationTarget(element, ConstantDocBuilder)
        is PsiExpression.Assignment -> StitcherDocumentationTarget(element, DependencyDocBuilder)
        else -> null
    }
}
