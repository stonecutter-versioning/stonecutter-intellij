package dev.kikugie.stonecutter.intellij.editor.index

import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileContent
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.uast.ULiteralExpression
import org.jetbrains.uast.toUElementOfType

private val KOTLIN_PLUGIN_ID by lazy { PluginId.findId("org.jetbrains.kotlin") }

private fun isKotlinPluginLoaded(): Boolean =
    if (KOTLIN_PLUGIN_ID == null) false
    else PluginManager.isPluginInstalled(KOTLIN_PLUGIN_ID)

object StitcherScriptIndexer : DataIndexer<StitcherIndexKey, Int, FileContent> {
    typealias Collector = MutableMap<StitcherIndexKey, Int>

    override fun map(input: FileContent): Map<StitcherIndexKey, Int> = buildMap {
        if (isKotlinPluginLoaded()) input.psiFile.accept(StringFinder(this))
    }

    private class StringFinder(val collector: Collector) : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            ProgressIndicatorProvider.checkCanceled()
            if (element is KtLiteralStringTemplateEntry) handleString(element)
            else element.acceptChildren(this)
        }

        /* TODO: Check if the string is in an array assignment operator, or in a function call.
        We can't build perfect indexes, since trying to resolve the caller type will throw IndexNotReadyException,
        so it will need to find "good enough" candidates.
        When resolving references, the full index should be available and the receiver can be properly checked.
         */
        private fun handleString(template: KtLiteralStringTemplateEntry) {
            val element = template.toUElementOfType<ULiteralExpression>()
        }
    }
}