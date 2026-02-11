package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType.SEM_VER
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherCompositeType.STR_VER
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiScope
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.compositeType

private fun AnnotationHolder.applyAttributes(element: PsiElement, key: TextAttributesKey) = newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
    .textAttributes(key).range(element).create()

class StitcherAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val token = element.antlrType
        if (token >= 0) return annotateTokenElement(token, element, holder)

        val composite = element.compositeType
        if (composite != null) return annotateCompositeElement(composite, element, holder)
    }

    private fun annotateCompositeElement(type: StitcherCompositeType, element: PsiElement, holder: AnnotationHolder) = when (type) {
        SEM_VER, STR_VER -> holder.applyAttributes(element, AttributeKeys.VERSION)
        else -> Unit
    }

    private fun annotateTokenElement(token: Int, element: PsiElement, holder: AnnotationHolder) = when (token) {
        StitcherLexer.IDENTIFIER -> when (val parent = element.parent) {
            is PsiSwap.Opener if element == parent.identifier -> holder.applyAttributes(element, AttributeKeys.SWAP)
            is PsiSwap.Opener -> holder.applyAttributes(element, AttributeKeys.LITERAL)
            is PsiSwap.Entry -> holder.applyAttributes(element, AttributeKeys.LITERAL)

            is PsiReplacement.Entry -> holder.applyAttributes(element, AttributeKeys.REPLACEMENT)

            is PsiExpression.Constant -> holder.applyAttributes(element, AttributeKeys.CONSTANT)
            is PsiExpression.Assignment -> holder.applyAttributes(element, AttributeKeys.DEPENDENCY)

            is PsiScope.Lookup -> holder.applyAttributes(element, AttributeKeys.LITERAL)
            else -> Unit
        }

        StitcherLexer.PLUS -> when (val parent = element.parent) {
            is PsiScope.Lookup -> holder.applyAttributes(element, AttributeKeys.OPERATOR)
            else -> Unit
        }
        else -> Unit
    }
}