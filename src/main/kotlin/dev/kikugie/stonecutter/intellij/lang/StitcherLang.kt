package dev.kikugie.stonecutter.intellij.lang

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.InjectableLanguage
import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.psi.templateLanguages.TemplateLanguage

object StitcherLang : Language("Stitcher"), InjectableLanguage, TemplateLanguage {
    private fun readResolve(): Any = StitcherLang
    val TEMPLATE_FILE: LanguageFileType = JavaFileType.INSTANCE
}