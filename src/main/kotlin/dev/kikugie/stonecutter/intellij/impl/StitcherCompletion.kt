package dev.kikugie.stonecutter.intellij.impl

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import dev.kikugie.stonecutter.intellij.lang.token.StitcherElement
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType

class StitcherCompletion : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(StitcherType.Primitive.IDENTIFIER), Identifier())
    }

    class Identifier : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val element = parameters.originalPosition?.parent as? StitcherElement.Reference<*> ?: return
            for (it in element.keys.getOrElse { emptyList() })
                result.addElement(LookupElementBuilder.create(it))
        }
    }
}