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
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapId
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.model.serialized.Replacement
import dev.kikugie.stonecutter.intellij.service.stonecutterNode

// TODO: move the missing value logic entirely to the inspection
class StitcherAnnotator: Annotator {
    private enum class ReferenceType(val attribute: TextAttributesKey, val variants: (SCProcessProperties) -> Collection<String>) {
        CONSTANT(AttributeKeys.CONSTANT, { it.constants.keys }),
        DEPENDENCY(AttributeKeys.DEPENDENCY, { it.dependencies.keys }),
        REPLACEMENT(AttributeKeys.REPLACEMENT, { it.replacements.mapNotNull(Replacement::identifier) }),
        SWAP(AttributeKeys.SWAP, { it.swaps.keys });
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