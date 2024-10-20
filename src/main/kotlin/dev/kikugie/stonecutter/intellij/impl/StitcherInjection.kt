package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.impl.FileTypeOverrider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor
import com.intellij.psi.util.elementType
import com.intellij.testFramework.LightVirtualFile
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.util.isStitcherComment

class StitcherInjector : LanguageInjector, DumbAware, InjectionBackgroundSuppressor {
    override fun getLanguagesToInject(
        host: PsiLanguageInjectionHost,
        registrar: InjectedLanguagePlaces,
    ) {
        if (!host.isStitcherComment) return
        val range = ElementManipulators.getValueTextRange(host)
        registrar.addPlace(StitcherLang, range, null, null)
    }
}

@Suppress("UnstableApiUsage")
class StitcherFileTypeOverrider : FileTypeOverrider {
    override fun getOverriddenFileType(file: VirtualFile): FileType? =
        if (file is LightVirtualFile && file.language == StitcherLang) StitcherFile.StitcherFileType else null
}