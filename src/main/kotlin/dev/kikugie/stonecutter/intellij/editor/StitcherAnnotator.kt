package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.psi.*
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.util.childrenSequence

class StitcherAnnotator: Annotator {
    private enum class ReferenceType(val attribute: TextAttributesKey, val variants: (SCProcessProperties) -> Collection<String>) {
        CONSTANT(AttributeKeys.CONSTANT, { it.constants.keys }),
        DEPENDENCY(AttributeKeys.DEPENDENCY, { it.dependencies.keys }),
        REPLACEMENT(AttributeKeys.REPLACEMENT, SCProcessProperties::replacements),
        SWAP(AttributeKeys.SWAP, SCProcessProperties::swaps);
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val reference = element.referenceType
        if (reference != null) annotateReference(element, holder, reference)
    }

    private val PsiElement.referenceType get() = when (this) {
        is StitcherConstant -> ReferenceType.CONSTANT
        is StitcherDependency -> ReferenceType.DEPENDENCY
        is StitcherReplacement -> ReferenceType.REPLACEMENT
        is StitcherSwapId -> ReferenceType.SWAP
        else -> null
    }

    private fun annotateReference(element: PsiElement, holder: AnnotationHolder, type: ReferenceType) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .textAttributes(type.attribute)
            .range(element)
            .create()

        val text = element.text
        val properties = element.stonecutterNode?.params
            ?: return
        if (text !in type.variants(properties)) holder
            .newAnnotation(HighlightSeverity.ERROR, "Invalid ${type.name.lowercase()} ID")
            .range(element)
            .create()
    }
}