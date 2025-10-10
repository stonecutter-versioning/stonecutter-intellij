package dev.kikugie.stonecutter.intellij.editor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import dev.kikugie.stonecutter.intellij.editor.documentation.*
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParserExtras
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.util.antlrRule
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

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

    private fun matchDocumentationTarget(element: PsiElement): StitcherDocumentationTarget<out ANTLRPsiNode>? = when (element.antlrRule) {
        StitcherParserExtras.RULE_conditionExpression_assignment ->
            StitcherDocumentationTarget(element as PsiExpression.Assignment, DependencyDocBuilder)

        StitcherParserExtras.RULE_conditionExpression_constant ->
            StitcherDocumentationTarget(element as PsiExpression.Constant, ConstantDocBuilder)

        StitcherParser.RULE_replacement ->
            StitcherDocumentationTarget(element as PsiReplacement, ReplacementDocBuilder)

        StitcherParser.RULE_swap ->
            StitcherDocumentationTarget(element as PsiSwap, SwapDocBuilder)

        else -> null
    }
}
