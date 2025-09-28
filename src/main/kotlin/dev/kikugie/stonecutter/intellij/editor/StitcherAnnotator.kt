package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSemanticVersion
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherStringVersion
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapArg
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapKey

private fun AnnotationHolder.applyAttributes(element: PsiElement, key: TextAttributesKey) = newSilentAnnotation(HighlightSeverity.TEXT_ATTRIBUTES)
    .textAttributes(key).range(element).create()

class StitcherAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) = when (element) {
        is StitcherStringVersion,
        is StitcherSemanticVersion -> holder.applyAttributes(element, AttributeKeys.VERSION)
        is StitcherSwapArg -> holder.applyAttributes(element, AttributeKeys.LITERAL)
        is StitcherSwapKey -> holder.applyAttributes(element, AttributeKeys.SWAP)
        is StitcherConstant -> holder.applyAttributes(element, AttributeKeys.CONSTANT)
        is StitcherDependency -> holder.applyAttributes(element, AttributeKeys.DEPENDENCY)
        is StitcherReplacement -> holder.applyAttributes(element, AttributeKeys.REPLACEMENT)
        else -> {
            // no-op
        }
    }
}