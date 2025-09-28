package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import dev.kikugie.stonecutter.intellij.editor.completion.StitcherPatterns
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapKey
import dev.kikugie.stonecutter.intellij.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

class StitcherCompletionContributor : CompletionContributor() {
    init {
        val targets = PlatformPatterns
            .psiElement(StitcherTokenTypes.LITERAL)

        register(targets.beforeLeaf(PlatformPatterns.psiElement(StitcherTokenTypes.BINARY))) { params, _, result ->
            val variants = params.getNode()?.params?.constants?.keys.orEmpty()
                .map(LookupElementBuilder::create)
            if (variants.isNotEmpty()) result.addAllElements(variants)
        }

        register(targets.beforeLeaf(PlatformPatterns.psiElement(StitcherTokenTypes.ASSIGN))) { params, _, result ->
            val variants = params.getNode()?.params?.dependencies?.keys.orEmpty()
                .map(LookupElementBuilder::create)
            if (variants.isNotEmpty()) result.addAllElements(variants)
        }

        register(targets.withParent(StitcherSwapKey::class.java)) { params, _, result ->
            val variants = params.getNode()?.params?.swaps.orEmpty()
                .map(LookupElementBuilder::create)
            if (variants.isNotEmpty()) result.addAllElements(variants)
        }

        // FIXME: It suggests constant names
        register(targets.withParent(StitcherReplacement::class.java)) { params, _, result ->
            val variants = params.getNode()?.params?.replacements.orEmpty()
                .map(LookupElementBuilder::create)
            if (variants.isNotEmpty()) result.addAllElements(variants)
        }

        register(
            targets
                .without(StitcherPatterns.beforeLeafCondition(StitcherTokenTypes.ASSIGN, StitcherTokenTypes.BINARY))
                .without(StitcherPatterns.afterLeafCondition(StitcherTokenTypes.ASSIGN))
        ) { params, _, result ->
            val params = params.getNode()?.params
            val constants = params?.constants?.keys.orEmpty()
            val dependencies = params?.dependencies?.keys.orEmpty()

            val variants = (constants + dependencies)
                .map(LookupElementBuilder::create)
            if (variants.isNotEmpty()) result.addAllElements(variants)
        }
    }

    private inline fun register(
        pattern: ElementPattern<out PsiElement>,
        crossinline action: (params: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) -> Unit
    ) {
        extend(CompletionType.BASIC, pattern, object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                action(parameters, context, result)
            }
        })
    }

    private fun CompletionParameters.getNode(): SCProjectNode? {
        val element = originalPosition ?: return null
        return element.stonecutterService.lookup.node(element)
    }
}