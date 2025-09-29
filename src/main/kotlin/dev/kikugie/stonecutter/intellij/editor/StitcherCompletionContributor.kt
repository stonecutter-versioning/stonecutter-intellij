package dev.kikugie.stonecutter.intellij.editor

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import dev.kikugie.commons.collections.ifNotEmpty
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes
import dev.kikugie.stonecutter.intellij.lang.access.VersionDefinition
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapKey
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

private val CompletionParameters.stonecutter: SCProcessProperties?
    get() = originalPosition?.stonecutterNode?.params

private inline fun <reified T : PsiElement> PsiElementPattern.Capture<*>.withParent() =
    withParent(T::class.java)

private inline fun <reified T : PsiElement> PsiElementPattern.Capture<*>.inside() =
    inside(T::class.java)

class StitcherCompletionContributor : CompletionContributor() {
    init {
        register(psiElement(StitcherTokenTypes.IDENTIFIER).withParent<StitcherReplacement>()) { params, _, result ->
            params.stonecutter?.replacements.orEmpty()
                .mapNotNull { LookupElementBuilder.create(it.identifier ?: return@mapNotNull null) }
                .ifNotEmpty(result::addAllElements)
        }

        register(psiElement(StitcherTokenTypes.IDENTIFIER).withParent<StitcherSwapKey>()) { params, _, result ->
            params.stonecutter?.swaps?.keys.orEmpty()
                .map(LookupElementBuilder::create)
                .ifNotEmpty(result::addAllElements)
        }

        register(psiElement(StitcherTokenTypes.IDENTIFIER).withParent<StitcherDependency>()) { params, _, result ->
            params.stonecutter?.dependencies?.keys.orEmpty()
                .map(LookupElementBuilder::create)
                .ifNotEmpty(result::addAllElements)
        }

        register(psiElement(StitcherTokenTypes.IDENTIFIER).withParent<StitcherConstant>()) { params, _, result ->
            params.stonecutter?.constants?.keys.orEmpty()
                .map(LookupElementBuilder::create)
                .ifNotEmpty(result::addAllElements)
            params.stonecutter?.dependencies?.keys.orEmpty()
                .map { LookupElementBuilder.create("$it:") }
                .ifNotEmpty(result::addAllElements)
        }

        // FIXME: Doesn't do shit
        register(psiElement().inside<VersionDefinition>()) { params, _, result ->
            val element = params.originalPosition ?: return@register
            val dependency = (element.parentOfType<StitcherAssignment>() ?: return@register)
                .dependency?.text.orEmpty()
            element
                .run { stonecutterNode?.siblings(stonecutterService.lookup).orEmpty().mapNotNull { it.params.dependencies[dependency] } }
                .distinct().map { LookupElementBuilder.create(it.value) }
                .toList().ifNotEmpty(result::addAllElements)
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
}