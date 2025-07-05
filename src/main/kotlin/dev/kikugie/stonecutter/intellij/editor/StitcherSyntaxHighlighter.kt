package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
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
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Colors

class StitcherSyntaxHighlighter : SyntaxHighlighterBase() {
    class Provider : EditorHighlighterProvider {
        override fun getEditorHighlighter(project: Project?, fileType: FileType, file: VirtualFile?, colors: EditorColorsScheme): EditorHighlighter {
            return LexerEditorHighlighter(StitcherSyntaxHighlighter(), colors)
        }
    }

    override fun getHighlightingLexer(): Lexer = StitcherLexer()

    override fun getTokenHighlights(token: IElementType?): Array<out TextAttributesKey> {
        val result = (token as? StitcherTokenType)?.matchColor()?.let { arrayOf(it) } ?: emptyArray()
        return result
    }

    private fun StitcherTokenType.matchColor(): TextAttributesKey? = when (this) {
        COND_MARKER, SWAP_MARKER, REPL_MARKER -> StitcherTextAttributesKeys.STITCHER_MARKER
        COMPARATOR, UNARY, BINARY, ASSIGN -> StitcherTextAttributesKeys.STITCHER_OPERATOR
        NUMERIC, DASH, PLUS, DOT -> StitcherTextAttributesKeys.STITCHER_NUMBER
        IDENTIFIER, LITERAL -> StitcherTextAttributesKeys.STITCHER_IDENTIFIER
        LEFT_BRACE, RIGHT_BRACE, OPENER, CLOSER -> StitcherTextAttributesKeys.STITCHER_BRACES
        SUGAR -> StitcherTextAttributesKeys.STITCHER_KEYWORD
        else -> null
    }
}