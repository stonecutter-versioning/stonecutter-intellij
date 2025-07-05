package dev.kikugie.stonecutter.intellij.lang

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.TemplateLanguageFileType
import com.intellij.psi.FileViewProvider
import javax.swing.Icon

class StitcherFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, StitcherLang) {
    override fun getFileType(): FileType = StitcherFileType
    object StitcherFileType : LanguageFileType(StitcherLang){
        override fun getName(): String = "Stitcher"
        override fun getDescription(): String = "Stitcher comments"
        override fun getDefaultExtension(): String = "stitcher"
        override fun getIcon(): Icon? = null
    }
}