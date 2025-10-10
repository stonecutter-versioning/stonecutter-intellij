package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import dev.kikugie.commons.collections.ifNotEmpty
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
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

private fun StitcherCompletionContributor.registerPatterns() {
    psiAntlrToken(StitcherParser.IDENTIFIER).withParent<PsiReplacement>() register { params, result ->
        params.stonecutter?.replacements.orEmpty()
            .mapNotNull { LookupElementBuilder.create(it.identifier ?: return@mapNotNull null) }
            .ifNotEmpty(result::addAllElements)
    }

    psiAntlrToken(StitcherParser.IDENTIFIER).withParent<PsiSwap>() register { params, result ->
        params.stonecutter?.swaps?.keys.orEmpty()
            .map(LookupElementBuilder::create)
            .ifNotEmpty(result::addAllElements)
    }

    psiAntlrToken(StitcherParser.IDENTIFIER).withParent<PsiExpression.Assignment>() register { params, result ->
        params.stonecutter?.dependencies?.keys.orEmpty()
            .map(LookupElementBuilder::create)
            .ifNotEmpty(result::addAllElements)
    }

    psiAntlrToken(StitcherParser.IDENTIFIER).withParent<PsiExpression.Constant>() register { params, result ->
        with(params.stonecutter ?: return@register) {
            dependencies[""]?.value
                ?.let { result.addElement(LookupElementBuilder.create(it)) }
            constants.keys
                .map(LookupElementBuilder::create)
                .ifNotEmpty(result::addAllElements)
            dependencies.keys
                .map { LookupElementBuilder.create("$it:") }
                .ifNotEmpty(result::addAllElements)
        }
    }

    psiAntlrToken(StitcherParser.NUMERIC).withParent(psiAntlrRule(StitcherParser.RULE_semanticVersion)) register { params, result ->
        params.stonecutter?.dependencies?.get("")?.value
            ?.let { result.addElement(LookupElementBuilder.create(it)) }
    }
}

class StitcherCompletionContributor : CompletionContributor() {
    init { registerPatterns() }

    internal inline infix fun ElementPattern<out PsiElement>.register(
        crossinline action: (params: CompletionParameters, result: CompletionResultSet) -> Unit
    ): Unit = extend(CompletionType.BASIC, this, object : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            action(parameters, result)
        }
    })
}