package dev.kikugie.stonecutter.intellij.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.kikugie.stonecutter.intellij.editor.documentation.ASTInspectionBuilder
import dev.kikugie.stonecutter.intellij.editor.documentation.StitcherDocumentationTarget
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

class StitcherDocumentationTargetProvider : DocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> =
        if (file.language != StitcherLang) emptyList()
        else file.findElementAt(offset)?.let(::documentationTargets).orEmpty()

    private fun documentationTargets(element: PsiElement): List<DocumentationTarget> = buildList {
        findDocumentationTarget(element)?.let(::add)
        if (ApplicationManager.getApplication().isInternal)
            StitcherDocumentationTarget(element.containingFile, ASTInspectionBuilder).let(::add)
    }

    private fun findDocumentationTarget(element: PsiElement): DocumentationTarget? = null
//        element.parents(true)
//        .firstNotNullOfOrNull(::matchDocumentationTarget)

//    private fun matchDocumentationTarget(element: PsiElement): StitcherDocumentationTarget<out PsiElement>? = when (element.antlrRule) {
//        StitcherParserExtras.RULE_assignmentExpression ->
//            StitcherDocumentationTarget(element as PsiExpression.Assignment, DependencyDocBuilder)
//
//        StitcherParserExtras.RULE_constantExpression ->
//            StitcherDocumentationTarget(element as PsiExpression.Constant, ConstantDocBuilder)
//
//        StitcherParser.RULE_replacementEntry ->
//            StitcherDocumentationTarget(element, ReplacementDocBuilder)
//
//        StitcherParserExtras.RULE_openerSwap ->
//            StitcherDocumentationTarget(element as PsiSwap.Identified, SwapDocBuilder)
//
//        else -> null
//    }
}
