package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType

/**
 * Annotator for the Stitcher language itself (not the host language).
 * This applies syntax highlighting to injected Stitcher language elements.
 */
class StitcherLanguageAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        // Handle leaf PSI elements (tokens)
        if (element is LeafPsiElement) {
            val elementType = element.elementType

            if (elementType is StitcherTokenType) {
                val textAttributesKey = elementType.getTextAttributesKey()
                if (textAttributesKey != null) {
                    val range = element.textRange

                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(range)
                        .textAttributes(textAttributesKey)
                        .create()
                }
            }
        }
    }

    private fun StitcherTokenType.getTextAttributesKey() =
        StitcherSyntaxHighlighter().getTokenHighlights(this).firstOrNull()
}