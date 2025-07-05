package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.EditorHighlighterProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenTypes.*

class StitcherSyntaxHighlighter : SyntaxHighlighterBase() {
    object AttributeKeys {
        @JvmField val MARKER = createTextAttributesKey("STONECUTTER_MARKER", DefaultLanguageHighlighterColors.KEYWORD)
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

    class Provider : EditorHighlighterProvider {
        override fun getEditorHighlighter(project: Project?, fileType: FileType, file: VirtualFile?, colors: EditorColorsScheme): EditorHighlighter =
            LexerEditorHighlighter(StitcherSyntaxHighlighter(), colors)
    }

    override fun getHighlightingLexer(): Lexer = StitcherLexer()

    override fun getTokenHighlights(token: IElementType?): Array<out TextAttributesKey> =
        (token as? StitcherTokenType)?.matchColor()?.let { arrayOf(it) } ?: emptyArray()

    private fun StitcherTokenType.matchColor(): TextAttributesKey? = when (this) {
        COND_MARKER, SWAP_MARKER, REPL_MARKER -> AttributeKeys.MARKER
        COMPARATOR, UNARY, BINARY, ASSIGN -> AttributeKeys.OPERATOR
        NUMERIC, DASH, PLUS, DOT -> AttributeKeys.NUMBER
        IDENTIFIER, LITERAL -> AttributeKeys.IDENTIFIER
        LEFT_BRACE, RIGHT_BRACE, OPENER, CLOSER -> AttributeKeys.BRACES
        SUGAR -> AttributeKeys.KEYWORD
        else -> null
    }
}