package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

class StitcherCompletionContributor : CompletionContributor() {
    init {
        val constant = PlatformPatterns
            .psiElement(StitcherTokenTypes.LITERAL)
            .inside(PlatformPatterns.psiElement(StitcherConstant::class.java))
        register(constant) { params, context, result ->
            val variants = params.getNode()?.params?.constants?.keys.orEmpty()
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