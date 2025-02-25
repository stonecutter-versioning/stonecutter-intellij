package dev.kikugie.stonecutter.intellij.lang

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.Language
import com.intellij.lang.LanguageUtil
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.LanguageSubstitutor
import com.intellij.testFramework.LightVirtualFile

class StitcherLanguageSubstitutor : LanguageSubstitutor() {
    override fun getLanguage(file: VirtualFile, project: Project): Language? =
        if (file.fileType is JavaFileType) StitcherLang else null
}