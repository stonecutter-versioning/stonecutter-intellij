package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.util.canHasStitcherCode
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings

class StitcherInjector : MultiHostInjector, DumbAware {
    private val comments: List<Class<out PsiElement>> = listOf(PsiCommentImpl::class.java)
    private val PsiElement.isStitcherComment: Boolean
        get() = this is PsiComment
            && (!StonecutterSettings.STATE.checkedLangInject || stonecutterService.lookup.nodes.isNotEmpty())
            && canHasStitcherCode

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context is PsiLanguageInjectionHost && context.isStitcherComment) registrar
            .startInjecting(StitcherLang)
            .addPlace(null, null, context, ElementManipulators.getValueTextRange(context))
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = comments
}