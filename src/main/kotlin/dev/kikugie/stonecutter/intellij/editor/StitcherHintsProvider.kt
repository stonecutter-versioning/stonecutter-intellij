package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.hints.declarative.*
import com.intellij.openapi.editor.Editor
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.descendants
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.parents
import com.intellij.psi.util.startOffset
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.lang.psi.PsiCondition
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterService

private class StitcherHintsCollector : SharedBypassCollector {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        val definition = element.takeAsOrNull<PsiComment>()?.commentDefinition?.element
            ?: return
        val component = definition.component?.takeAsOrNull<PsiCondition>()
            ?: return

        val offset = element.startOffset + ElementManipulators.getOffsetInElement(element)
        collectFromCondition(component, sink, offset)
    }

    private fun collectFromCondition(component: PsiCondition, sink: InlayTreeSink, offset: Int) {
        for (element in component.descendants(canGoInside = ::isVisitable)) when (element) {
            is PsiExpression.Constant -> collectFromConstant(element, sink, offset)
            is PsiExpression.Assignment -> collectFromAssignment(element, sink, offset)
        }
    }

    private fun collectFromConstant(constant: PsiExpression.Constant, sink: InlayTreeSink, offset: Int): Unit =
        collectFromResolved(constant, sink, offset, constant.value {
            var bool = constants[it] ?: return@value null
            for (parent in constant.parents(false))
                if (parent !is PsiExpression.Unary) break
                else bool = !bool
            bool
        })

    private fun collectFromAssignment(assignment: PsiExpression.Assignment, sink: InlayTreeSink, offset: Int): Unit =
        collectFromResolved(assignment, sink, offset, assignment.value { dependencies[it]?.value })

    private fun collectFromResolved(parameter: PsiElement, sink: InlayTreeSink, offset: Int, value: String?) {
        if (value == null) return
        val position = InlineInlayPosition(parameter.endOffset + offset, true)
        val format = HintFormat(HintColorKind.TextWithoutBackground, HintFontSize.ABitSmallerThanInEditor, HintMarginPadding.MarginAndSmallerPadding)
        sink.addPresentation(position, hintFormat = format) { text("($value)") }
    }

    private fun isVisitable(element: PsiElement): Boolean = when (element) {
        is PsiExpression.Constant,
        is PsiExpression.Assignment -> false

        else -> true
    }

    private inline fun PsiElement.value(provider: SCProcessProperties.(String) -> Any?): String? =
        stonecutterService.lookup.node(this)?.params?.provider(text)?.toString()
}

class StitcherHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = StitcherHintsCollector()
}