package dev.kikugie.stonecutter.intellij.impl

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType
import javax.swing.Icon

class StitcherHighlighting(project: Project?, file: VirtualFile?, scheme: EditorColorsScheme) : LayeredLexerEditorHighlighter(Highlighter(), scheme) {
    init {
        val type = if (project == null || file == null) FileTypes.PLAIN_TEXT
        else TemplateDataLanguageMappings.getInstance(project).getMapping(file)
            ?.associatedFileType ?: StitcherLang.TEMPLATE_FILE
        val highlighter = requireNotNull(SyntaxHighlighterFactory.getSyntaxHighlighter(type, project, file))
            { "No syntax highlighter factory found for ${type.displayName} file ${file?.path}" }
        registerLayer(StitcherType.Component.DEFINITION, LayerDescriptor(highlighter, ""))
    }

    object Constants {
        val SUGAR_KEY = createTextAttributesKey("STITCHER_SUGAR", DefaultLanguageHighlighterColors.KEYWORD)
        val IDENTIFIER_KEY = createTextAttributesKey("STITCHER_IDENTIFIER", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
        val PREDICATE_KEY = createTextAttributesKey("STITCHER_PREDICATE", DefaultLanguageHighlighterColors.NUMBER)
        val MARKER_KEY = createTextAttributesKey("STITCHER_MARKER", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    }

    class Provider : EditorHighlighterProvider {
        override fun getEditorHighlighter(project: Project?, type: FileType, file: VirtualFile?, colors: EditorColorsScheme): EditorHighlighter? =
            StitcherHighlighting(project, file, colors)
    }

    class Highlighter : SyntaxHighlighterBase(), DumbAware {
        override fun getHighlightingLexer(): Lexer = StitcherLexer()

        override fun getTokenHighlights(type: IElementType?): Array<out TextAttributesKey?> = when (type) {
            is StitcherType.Sugar -> Constants.SUGAR_KEY
            is StitcherType.Marker -> Constants.MARKER_KEY
            is StitcherType.Scope -> DefaultLanguageHighlighterColors.BRACKETS
            is StitcherType.Reference,
            StitcherType.Primitive.IDENTIFIER -> Constants.IDENTIFIER_KEY
            StitcherType.Primitive.PREDICATE -> Constants.PREDICATE_KEY
            StitcherType.Operator.LPAREN,
            StitcherType.Operator.RPAREN -> DefaultLanguageHighlighterColors.BRACES
            else -> null
        }.let(SyntaxHighlighterBase::pack)
    }

    class Settings : ColorSettingsPage {
        override fun getAttributeDescriptors(): Array<AttributesDescriptor> = Constants.DESCRIPTORS

        override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

        override fun getDisplayName(): String = "Stitcher"

        override fun getIcon(): Icon? = null

        override fun getHighlighter(): SyntaxHighlighter = Highlighter()

        override fun getDemoText(): String = """
        //? if condition {
    """.trimIndent()

        override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

        object Constants {
            val DESCRIPTORS = arrayOf(
                AttributesDescriptor("Condition sugar", StitcherHighlighting.Constants.SUGAR_KEY),
                AttributesDescriptor("Type marker", StitcherHighlighting.Constants.MARKER_KEY),
                AttributesDescriptor("Identifier", StitcherHighlighting.Constants.IDENTIFIER_KEY),
                AttributesDescriptor("Predicate", StitcherHighlighting.Constants.PREDICATE_KEY)
            )
        }
    }
}