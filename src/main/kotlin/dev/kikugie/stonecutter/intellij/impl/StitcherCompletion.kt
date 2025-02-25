package dev.kikugie.stonecutter.intellij.impl

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherTypes

class StitcherCompletionContributor : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(StitcherTypes.Primitive.CONSTANT),
            StitcherCompletionProvider(StitcherTypes.Primitive.CONSTANT )
        )
    }

    class StitcherCompletionProvider(val type: StitcherType) : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val element = parameters.position
            val file = parameters.originalFile
            val params = file.project.stonecutterService.lookup(file).active.getOrNull()?.parameters
                ?: return
            val options = when (type) {
                StitcherTypes.Primitive.CONSTANT  -> params.constants.keys
                StitcherTypes.Primitive.DEPENDENCY -> params.dependencies.keys
                StitcherTypes.Primitive.SWAP  -> params.swaps.keys
                else -> return
            }.map {
                LookupElementBuilder.create(it)
            }
            result.addAllElements(options)
        }
    }
}