package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParserExtras
import dev.kikugie.stonecutter.intellij.lang.util.antlrRule
import dev.kikugie.stonecutter.intellij.lang.util.antlrType

private fun AnnotationHolder.applyAttributes(element: PsiElement, key: TextAttributesKey) = newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
    .textAttributes(key).range(element).create()

class StitcherAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val token = element.antlrType
        if (token >= 0) return annotateTokenElement(token, element, holder)

        val rule = element.antlrRule
        if (rule >= 0) return annotateRuleElement(rule, element, holder)
    }

    private fun annotateTokenElement(token: Int, element: PsiElement, holder: AnnotationHolder) = when (token) {
        StitcherParser.IDENTIFIER -> when (element.parent.antlrRule) {
            StitcherParser.RULE_swap -> holder.applyAttributes(element, AttributeKeys.SWAP)
            StitcherParser.RULE_replacement -> holder.applyAttributes(element, AttributeKeys.REPLACEMENT)
            StitcherParserExtras.RULE_conditionExpression_constant -> holder.applyAttributes(element, AttributeKeys.CONSTANT)
            StitcherParserExtras.RULE_conditionExpression_assignment -> holder.applyAttributes(element, AttributeKeys.DEPENDENCY)
            else -> Unit
        }
        else -> Unit
    }

    private fun annotateRuleElement(rule: Int, element: PsiElement, holder: AnnotationHolder) = when (rule) {
        StitcherParser.RULE_semanticVersion,
        StitcherParser.RULE_stringVersion -> holder.applyAttributes(element, AttributeKeys.VERSION)
        StitcherParser.RULE_swapArguments -> holder.applyAttributes(element, AttributeKeys.LITERAL)
        else -> Unit
    }
}