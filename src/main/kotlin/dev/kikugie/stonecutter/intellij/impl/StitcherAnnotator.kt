package dev.kikugie.stonecutter.intellij.impl

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.token.StitcherElement
import dev.kikugie.stonecutter.intellij.lang.token.StitcherElement.Companion.present
import dev.kikugie.stonecutter.intellij.lang.token.StitcherElement.Companion.type

class StitcherAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        validateParameters(element, holder)
    }

    private fun validateParameters(element: PsiElement, holder: AnnotationHolder) {
        when (element as? StitcherElement.Reference<*>?) {
            null -> {}
            is StitcherElement.Reference.Constant,
            is StitcherElement.Reference.Dependency,
            is StitcherElement.Reference.Swap ->
                if (element.present == false) reportReference(element, holder)
            is StitcherElement.Reference.Ambiguous -> {
                element as StitcherElement.Reference.Ambiguous
                val text = element.text
                if (text in element.constants.getOrElse { listOf(text) }) return // Is a constant
                if (text in element.dependencies.getOrElse { emptyList() }) return holder
                    .newAnnotation(HighlightSeverity.ERROR, "Missing version predicate")
                    .afterEndOfLine()
                    .create()
                reportReference(element, holder)
            }
        }
    }

    private fun reportReference(element: StitcherElement.Reference<*>, holder: AnnotationHolder) =
        holder.newAnnotation(HighlightSeverity.ERROR, "Invalid ${element.type} reference").range(element).create()
}