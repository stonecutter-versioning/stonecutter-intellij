package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import dev.kikugie.commons.text.getOrDefault
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.settings.StonecutterOptions
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings

private val PREFIXES = charArrayOf('?', '$', '~')

class StitcherInjector : MultiHostInjector, DumbAware {
    private val comments: List<Class<out PsiElement>> = listOf(PsiCommentImpl::class.java)
    private val PsiElement.isStitcherComment: Boolean get() = when {
        this !is PsiComment -> false
        StonecutterSettings.STATE.checkedLangInject
            && stonecutterService.lookup.nodes.isEmpty() -> false
        else -> ElementManipulators.getValueText(this).getOrDefault(0) in PREFIXES
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context is PsiLanguageInjectionHost && context.isStitcherComment) registrar
            .startInjecting(StitcherLang)
            .addPlace(null, null, context, ElementManipulators.getValueTextRange(context))
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = comments
}