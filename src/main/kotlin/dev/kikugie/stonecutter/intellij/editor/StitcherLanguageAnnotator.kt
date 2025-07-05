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

        // Also handle composite elements by recursing into their children
        element.children.forEach { child ->
            annotate(child, holder)
        }
    }

    private fun StitcherTokenType.getTextAttributesKey() = when (this) {
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.COND_MARKER,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.SWAP_MARKER,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.REPL_MARKER -> StitcherTextAttributesKeys.STITCHER_MARKER

        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.SUGAR -> StitcherTextAttributesKeys.STITCHER_KEYWORD

        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.COMPARATOR,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.BINARY,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.UNARY,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.ASSIGN -> StitcherTextAttributesKeys.STITCHER_OPERATOR

        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.NUMERIC,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.DASH,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.PLUS,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.DOT -> StitcherTextAttributesKeys.STITCHER_NUMBER

        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.LITERAL -> StitcherTextAttributesKeys.STITCHER_DEPENDENCY
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.IDENTIFIER -> StitcherTextAttributesKeys.STITCHER_IDENTIFIER

        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.LEFT_BRACE,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.RIGHT_BRACE,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.OPENER,
        dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.CLOSER -> StitcherTextAttributesKeys.STITCHER_BRACES

        else -> null
    }
}