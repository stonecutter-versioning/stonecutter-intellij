package dev.kikugie.stonecutter.intellij.lang.file

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.LanguageSubstitutors
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.ConfigurableTemplateLanguageFileViewProvider
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.psi.templateLanguages.TemplateLanguage
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.token.StitcherTypes
import java.util.concurrent.ConcurrentHashMap

class StitcherFileViewProvider(
    manager: PsiManager,
    file: VirtualFile,
    physical: Boolean,
    private val baseLanguage: Language,
    private val templateLanguage: Language = getTemplateDataLanguange(manager, file)
) :
    MultiplePsiFilesPerDocumentFileViewProvider(manager, file, physical),
    ConfigurableTemplateLanguageFileViewProvider {

    private val Language.parserDefinition
        get() =
            if (isKindOf(baseLanguage)) LanguageParserDefinitions.INSTANCE.forLanguage(if (`is`(baseLanguage)) this else baseLanguage!!)
            else LanguageParserDefinitions.INSTANCE.forLanguage(this)

    override fun supportsIncrementalReparse(rootLanguage: Language): Boolean = false
    override fun getBaseLanguage(): Language = baseLanguage
    override fun getTemplateDataLanguage(): Language = templateLanguage
    override fun getLanguages(): Set<Language?> = setOf(baseLanguage, templateLanguage)
    override fun cloneInner(file: VirtualFile): MultiplePsiFilesPerDocumentFileViewProvider =
        StitcherFileViewProvider(manager, file, false, baseLanguage, templateLanguage)

    @Suppress("UnstableApiUsage")
    override fun getContentElementType(language: Language): IElementType? =
        if (!language.`is`(templateLanguage)) null
        else getTemplateDataElementType(baseLanguage)


    override fun createFile(lang: Language): PsiFile? {
        val parser = lang.parserDefinition ?: return null
        return when {
            lang.`is`(templateDataLanguage) -> parser.createFile(this).apply {
                getContentElementType(lang)?.also {
                    (this as PsiFileImpl).contentElementType = it
                }
            }

            lang.isKindOf(baseLanguage) -> parser.createFile(this)
            else -> null
        }
    }

    class Factory : FileViewProviderFactory {
        override fun createFileViewProvider(file: VirtualFile, language: Language, manager: PsiManager, physical: Boolean): FileViewProvider {
            require(language.isKindOf(StitcherLang)) { "Provided language is not Stitcher - ${language.displayName}" }
            return StitcherFileViewProvider(manager, file, physical, language)
        }
    }

    private companion object {
        val DATA_MAPPINGS: MutableMap<String, TemplateDataElementType> = ConcurrentHashMap()

        fun getTemplateDataElementType(lang: Language) = DATA_MAPPINGS.getOrPut(lang.id) {
            TemplateDataElementType("StitcherTemplateData", lang, StitcherTypes.CONTENT_ELEMENT, StitcherTypes.OUTER_ELEMENT)
        }

        fun getTemplateDataLanguange(manager: PsiManager, file: VirtualFile): Language {
            val dataLanguage = TemplateDataLanguageMappings.getInstance(manager.project).getMapping(file)
                ?: guessTemplateLanguage(file)
            val substituteLanguage = LanguageSubstitutors.getInstance()
                .substituteLanguage(dataLanguage, file, manager.project)
            return if (substituteLanguage in TemplateDataLanguageMappings.getTemplateableLanguages()) substituteLanguage
            else dataLanguage
        }

        fun guessTemplateLanguage(file: VirtualFile): Language =
            (guessTemplateFileType(file) as? LanguageFileType)?.language
                ?.takeUnless { it is TemplateLanguage || it.isKindOf(StitcherLang) }
                ?: StitcherLang.TEMPLATE_FILE.language

        fun guessTemplateFileType(file: VirtualFile?): FileType? =
            if (file == null || file is VirtualFileWindow) null
            else file.extension?.let(FileTypeManager.getInstance()::getFileTypeByExtension)
                ?.takeUnless { it == UnknownFileType.INSTANCE }
    }
}