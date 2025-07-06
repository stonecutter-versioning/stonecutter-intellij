package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

class StitcherInjector : MultiHostInjector, DumbAware {
    private val comments: List<Class<out PsiElement>> = listOf(PsiCommentImpl::class.java)
    private val PsiElement.isStitcherComment: Boolean get() {
        if (this !is PsiComment) return false
        val char = ElementManipulators.getValueText(this).firstOrNull()
        return char == '?' || char == '$' || char == '~'
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context is PsiLanguageInjectionHost && context.isStitcherComment) registrar
            .startInjecting(StitcherLang)
            .addPlace(null, null, context, ElementManipulators.getValueTextRange(context))
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = comments
}