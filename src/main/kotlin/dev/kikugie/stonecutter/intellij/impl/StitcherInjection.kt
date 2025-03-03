package dev.kikugie.stonecutter.intellij.impl

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor
import com.intellij.testFramework.LightVirtualFile
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.util.isStitcherComment

class StitcherInjector : MultiHostInjector, InjectionBackgroundSuppressor {
    private val comments: List<Class<out PsiElement>> = listOf(PsiCommentImpl::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        if (context is PsiLanguageInjectionHost && context.isStitcherComment) registrar
            .startInjecting(StitcherLang)
            .addPlace(null, null, context, ElementManipulators.getValueTextRange(context))
            .doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = comments
}

@Suppress("UnstableApiUsage")
class StitcherFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? =
        if (file is LightVirtualFile && file.language == StitcherLang) StitcherFile.StitcherFileType else null
}