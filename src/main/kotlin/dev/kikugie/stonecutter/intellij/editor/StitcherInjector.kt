package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

class StitcherInjector : MultiHostInjector, InjectionBackgroundSuppressor, DumbAware {

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context !is PsiComment) return

        val text = context.text

        // Check for Stitcher patterns and extract content
        val stitcherContent = when {
            // Block comments: /*? content */ or /*$ content */ or /*~ content */
            text.startsWith("/*") && text.endsWith("*/") && text.length > 4 -> {
                val inner = text.substring(2, text.length - 2) // Remove "/*" and "*/"
                if (inner.startsWith("?") || inner.startsWith("$") || inner.startsWith("~")) {
                    inner
                } else null
            }
            // Line comments: //? content or //$ content or //~ content
            text.startsWith("//") && text.length > 3 -> {
                val inner = text.substring(2) // Remove "//"
                if (inner.startsWith("?") || inner.startsWith("$") || inner.startsWith("~")) {
                    inner
                } else null
            }
            else -> null
        }

        if (stitcherContent == null) return

        // Check if this comment can be used as an injection host
        if (context !is PsiLanguageInjectionHost) return

        // Calculate the range for injection
        val prefixLength = if (text.startsWith("/*")) 2 else 2 // "/*" or "//"
        val suffixLength = if (text.startsWith("/*")) 2 else 0 // "*/" or nothing

        val injectionRange = TextRange(prefixLength, text.length - suffixLength)

        // Validate the range
        if (injectionRange.startOffset >= injectionRange.endOffset) return

        try {
            registrar
                .startInjecting(StitcherLang)
                .addPlace(null, null, context, injectionRange)
                .doneInjecting()
        } catch (e: Exception) {
            // Silently fail if injection doesn't work
        }
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(PsiComment::class.java)
    }
}