package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.psi.*
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.util.childrenSequence

class StitcherAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) = when (element) {
//        is StitcherCondition -> printTree(element).let(::println)
        is StitcherConstant -> annotateReference(element, holder, "constant", element.getKeys())
        is StitcherDependency -> annotateReference(element, holder, "dependency", element.getKeys())
        is StitcherSwapId -> annotateReference(element, holder, "swap", element.getKeys())
        is StitcherReplacement -> annotateReference(element, holder, "replacement", element.getKeys())
        else -> Unit
    }

    private fun annotateReference(element: PsiElement, holder: AnnotationHolder, type: String, keys: Set<String>?) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .textAttributes(DefaultLanguageHighlighterColors.CONSTANT)
            .range(element)
            .create()

        val text = element.text
        if (keys == null || text in keys) return
        holder.newAnnotation(HighlightSeverity.ERROR, "Invalid $type ID")
            .range(element)
            .create()
    }

    private fun StitcherConstant.getKeys() = stonecutterService.lookup.node(this)?.params?.constants?.keys
    private fun StitcherDependency.getKeys() = stonecutterService.lookup.node(this)?.params?.dependencies?.keys
    private fun StitcherSwapId.getKeys() = stonecutterService.lookup.node(this)?.params?.swaps
    private fun StitcherReplacement.getKeys() = stonecutterService.lookup.node(this)?.params?.replacements

    private fun printTree(em: PsiElement): String = buildString {
        appendLine("${em.elementType} (${em::class.simpleName}) - '${em.text}'")
        for (it in em.childrenSequence) appendLine(printTree(it).prependIndent("| "))
    }
}