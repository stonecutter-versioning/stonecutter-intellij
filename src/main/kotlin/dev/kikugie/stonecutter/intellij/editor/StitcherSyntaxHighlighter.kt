package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileTypes.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.templateLanguages.TemplateDataLanguageMappings
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherFile
import dev.kikugie.stonecutter.intellij.lang.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.*

private val APPLICABLE_TYPES = arrayOf(CONDITION, SWAP, REPLACEMENT_ID)

private fun StitcherSyntaxHighlighter.TemplateHighlighter.configure(project: Project?, file: VirtualFile?) {
    val type = if (project == null || file == null) FileTypes.PLAIN_TEXT
    else TemplateDataLanguageMappings.getInstance(project).getMapping(file)
        ?.associatedFileType ?: StitcherFile.StitcherFileType.INSTANCE
    val outer = SyntaxHighlighterFactory.getSyntaxHighlighter(type, project, file)
        ?.let { LayerDescriptor(it, "", TextAttributesKey.createTempTextAttributesKey("EMPTY", TextAttributes.ERASE_MARKER)) }
    if (outer != null) for (it in APPLICABLE_TYPES)
        registerLayer(it, outer)
}

class StitcherSyntaxHighlighter : SyntaxHighlighterBase() {
    object AttributeKeys {
        @JvmField val MARKER = createTextAttributesKey("STONECUTTER_MARKER", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        @JvmField val COND_MARKER = createTextAttributesKey("STONECUTTER_COND_MARKER", MARKER)
        @JvmField val SWAP_MARKER = createTextAttributesKey("STONECUTTER_SWAP_MARKER", MARKER)
        @JvmField val REPL_MARKER = createTextAttributesKey("STONECUTTER_REPL_MARKER", MARKER)

        @JvmField val KEYWORD = createTextAttributesKey("STONECUTTER_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        @JvmField val OPERATOR = createTextAttributesKey("STONECUTTER_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        @JvmField val NUMBER = createTextAttributesKey("STONECUTTER_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        @JvmField val BRACES = createTextAttributesKey("STONECUTTER_BRACES", DefaultLanguageHighlighterColors.BRACES)

        @JvmField val IDENTIFIER = createTextAttributesKey("STONECUTTER_IDENTIFIER", DefaultLanguageHighlighterColors.CONSTANT)
        @JvmField val CONSTANT = createTextAttributesKey("STONECUTTER_CONSTANT", IDENTIFIER)
        @JvmField val DEPENDENCY = createTextAttributesKey("STONECUTTER_DEPENDENCY", IDENTIFIER)
        @JvmField val REPLACEMENT = createTextAttributesKey("STONECUTTER_REPLACEMENT", IDENTIFIER)
        @JvmField val SWAP = createTextAttributesKey("STONECUTTER_SWAP", IDENTIFIER)
    }

    object Attribute : (IElementType?) -> TextAttributesKey? {
        override fun invoke(type: IElementType?): TextAttributesKey? =
            (type as? StitcherTokenType)?.matchColor()

        private fun StitcherTokenType.matchColor(): TextAttributesKey? = when (this) {
            COND_MARKER -> AttributeKeys.COND_MARKER
            SWAP_MARKER -> AttributeKeys.SWAP_MARKER
            REPL_MARKER -> AttributeKeys.REPL_MARKER
            COMPARATOR, UNARY, BINARY, ASSIGN -> AttributeKeys.OPERATOR
            NUMERIC, DASH, PLUS, DOT -> AttributeKeys.NUMBER
            IDENTIFIER, LITERAL -> AttributeKeys.IDENTIFIER
            LEFT_BRACE, RIGHT_BRACE, OPENER, CLOSER -> AttributeKeys.BRACES
            SUGAR -> AttributeKeys.KEYWORD
            else -> null
        }
    }

    class TemplateHighlighter(project: Project?, file: VirtualFile?, scheme: EditorColorsScheme) :
        LayeredLexerEditorHighlighter(StitcherSyntaxHighlighter(), scheme) {
        init {
            configure(project, file)
        }
    }

    class Provider : SyntaxHighlighterFactory(), EditorHighlighterProvider {
        override fun getSyntaxHighlighter(project: Project?, file: VirtualFile?): SyntaxHighlighter =
            StitcherSyntaxHighlighter()

        override fun getEditorHighlighter(project: Project?, type: FileType, file: VirtualFile?, colors: EditorColorsScheme): EditorHighlighter =
            TemplateHighlighter(project, file, colors)
    }

    override fun getHighlightingLexer(): Lexer = StitcherLexer()

    override fun getTokenHighlights(type: IElementType?): Array<out TextAttributesKey> =
        Attribute(type)?.let { arrayOf(it) } ?: emptyArray()
}