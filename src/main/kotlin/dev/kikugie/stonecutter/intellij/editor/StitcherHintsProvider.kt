package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.hints.declarative.HintColorKind
import com.intellij.codeInsight.hints.declarative.HintFontSize
import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.HintMarginPadding
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.openapi.editor.Editor
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.descendants
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.prevLeaf
import com.intellij.psi.util.prevLeafs
import com.intellij.psi.util.startOffset
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.access.ConditionDefinition
import dev.kikugie.stonecutter.intellij.lang.access.ScopeDefinition
import dev.kikugie.stonecutter.intellij.lang.access.VersionDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.util.filterNotWhitespace

class StitcherHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = Collector()

    private class Collector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            if (element is PsiComment) element.commentDefinition?.element?.let {
                // Carry the start of this comment because it doesn't work for injected languages
                val offset = element.startOffset + ElementManipulators.getOffsetInElement(element)
                collectFromDefinition(it, sink, offset)
            }
        }


        private fun collectFromDefinition(definition: ScopeDefinition, sink: InlayTreeSink, offset: Int) {
            if (definition is ConditionDefinition) for (it in definition.descendants { it !is VersionDefinition })
                collectFromParameter(it, sink, offset)
        }

        private fun collectFromParameter(parameter: PsiElement, sink: InlayTreeSink, offset: Int) = when (parameter) {
            is StitcherConstant -> collectFromResolved(parameter, sink, offset, parameter.value {
                val value = constants[it] ?: return@value null
                // Inverted constants showing the opposite hint would be inconvenient
                var modifier = true
                for (it in parameter.prevLeafs.filterNotWhitespace())
                    if (it.elementType == StitcherTokenTypes.UNARY) modifier = !modifier
                    else break
                if (modifier) value else !value
            })
            is StitcherDependency -> collectFromResolved(parameter, sink, offset, parameter.value { dependencies[it] })
            else -> {}
        }

        private fun collectFromResolved(parameter: PsiElement, sink: InlayTreeSink, offset: Int, value: String?) {
            if (value == null) return
            val position = InlineInlayPosition(parameter.endOffset + offset, true)
            val format = HintFormat(HintColorKind.TextWithoutBackground, HintFontSize.ABitSmallerThanInEditor, HintMarginPadding.MarginAndSmallerPadding)
            sink.addPresentation(position, hintFormat = format) { text("($value)") }
        }

        private inline fun PsiElement.value(provider: SCProcessProperties.(String) -> Any?): String? =
            stonecutterService.lookup.node(this)?.params?.provider(text)?.toString()
    }
}