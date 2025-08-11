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

@Deprecated("Currently has no use")
class StitcherAnnotator: Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
    }
}