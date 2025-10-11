package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder.create
import com.intellij.openapi.project.DumbAware
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import dev.kikugie.commons.collections.ifNotEmpty
import dev.kikugie.stonecutter.intellij.StonecutterIcons.Reference
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterNode

private val CompletionParameters.stonecutter: SCProcessProperties?
    get() = originalPosition?.stonecutterNode?.params

private fun psiAntlrToken(type: Int): PsiElementPattern.Capture<PsiElement> =
    psiElement(StitcherLang.tokenTypeOf(type))

private fun psiAntlrRule(type: Int): PsiElementPattern.Capture<PsiElement> =
    psiElement(StitcherLang.ruleTypeOf(type))

private inline fun <reified T : PsiElement> psiElement(): PsiElementPattern.Capture<T> =
    psiElement(T::class.java)

private inline fun <reified T : PsiElement> PsiElementPattern.Capture<out PsiElement>.withParent() =
    withParent(T::class.java)

private inline fun <reified T : PsiElement> PsiElementPattern.Capture<out PsiElement>.inside() =
    inside(T::class.java)

private fun formatSwap(value: String, length: Int): String {
    val line = value.lineSequence().first()
    return if (line.length == value.length && line.length <= length) line
    else line.take(length - 3) + "..."
}

private fun StitcherCompletionContributor.registerPatterns() {
    psiAntlrToken(StitcherLexer.IDENTIFIER).withParent<PsiReplacement>() register { params, result ->
        params.stonecutter?.replacements.orEmpty().mapNotNull {
            create(it.identifier ?: return@mapNotNull null).withIcon(Reference.REPLACEMENT)
        }.ifNotEmpty(result::addAllElements)
    }

    psiAntlrToken(StitcherLexer.IDENTIFIER).withParent<PsiSwap>() register { params, result ->
        params.stonecutter?.swaps.orEmpty().map { (k, v) ->
            create(k).withIcon(Reference.SWAP).withTailText(" " + formatSwap(k, 30))
        }.ifNotEmpty(result::addAllElements)
    }

    psiAntlrToken(StitcherLexer.IDENTIFIER).withParent<PsiExpression.Assignment>() register { params, result ->
        params.stonecutter?.dependencies.orEmpty().map { (k, v) ->
            create(k).withIcon(Reference.DEPENDENCY).withTailText(" " + v.value)
        }.ifNotEmpty(result::addAllElements)
    }

    psiAntlrToken(StitcherLexer.IDENTIFIER).withParent<PsiExpression.Constant>() register { params, result ->
        with(params.stonecutter ?: return@register) {
            constants.map { (k, v) ->
                create(k).withIcon(Reference.CONSTANT).withTailText(" " + v)
            }.ifNotEmpty(result::addAllElements)
            dependencies.map { (k, v) ->
                create("$k:").withIcon(Reference.DEPENDENCY).withTailText(" " + v.value)
            }.ifNotEmpty(result::addAllElements)
        }
    }
}

class StitcherCompletionContributor : CompletionContributor(), DumbAware {
    init { registerPatterns() }

    internal inline infix fun ElementPattern<out PsiElement>.register(
        crossinline action: (params: CompletionParameters, result: CompletionResultSet) -> Unit
    ): Unit = extend(CompletionType.BASIC, this, object : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            action(parameters, result)
        }
    })
}
