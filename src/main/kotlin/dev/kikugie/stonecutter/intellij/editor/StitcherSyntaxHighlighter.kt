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
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import org.antlr.intellij.adaptor.lexer.ANTLRLexerAdaptor

private fun StitcherSyntaxHighlighter.TemplateHighlighter.configure(project: Project?, file: VirtualFile?) {
    val type = if (project == null || file == null) FileTypes.PLAIN_TEXT
    else TemplateDataLanguageMappings.getInstance(project).getMapping(file)
        ?.associatedFileType ?: StitcherFile.StitcherFileType.INSTANCE
    val outer = SyntaxHighlighterFactory.getSyntaxHighlighter(type, project, file)
        ?.let { LayerDescriptor(it, "", TextAttributesKey.createTempTextAttributesKey("EMPTY", TextAttributes.ERASE_MARKER)) }
    if (outer != null) registerLayer(StitcherLang.ruleTypeOf(StitcherParser.RULE_definition), outer)
}

class StitcherSyntaxHighlighter : SyntaxHighlighterBase() {
    object AttributeKeys {
        @JvmField val MARKER = createTextAttributesKey("STONECUTTER_MARKER", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        @JvmField val COND_MARKER = createTextAttributesKey("STONECUTTER_COND_MARKER", MARKER)
        @JvmField val SWAP_MARKER = createTextAttributesKey("STONECUTTER_SWAP_MARKER", MARKER)
        @JvmField val REPL_MARKER = createTextAttributesKey("STONECUTTER_REPL_MARKER", MARKER)

        @JvmField val KEYWORD = createTextAttributesKey("STONECUTTER_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        @JvmField val OPERATOR = createTextAttributesKey("STONECUTTER_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        @JvmField val VERSION = createTextAttributesKey("STONECUTTER_VERSION", DefaultLanguageHighlighterColors.NUMBER)
        @JvmField val LITERAL = createTextAttributesKey("STONECUTTER_LITERAL", DefaultLanguageHighlighterColors.STRING)
        @JvmField val BRACES = createTextAttributesKey("STONECUTTER_BRACES", DefaultLanguageHighlighterColors.BRACES)
        @JvmField val COMMENT = createTextAttributesKey("STONECUTTER_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

        @JvmField val IDENTIFIER = createTextAttributesKey("STONECUTTER_IDENTIFIER", DefaultLanguageHighlighterColors.CONSTANT)
        @JvmField val CONSTANT = createTextAttributesKey("STONECUTTER_CONSTANT", IDENTIFIER)
        @JvmField val DEPENDENCY = createTextAttributesKey("STONECUTTER_DEPENDENCY", IDENTIFIER)
        @JvmField val REPLACEMENT = createTextAttributesKey("STONECUTTER_REPLACEMENT", IDENTIFIER)
        @JvmField val SWAP = createTextAttributesKey("STONECUTTER_SWAP", IDENTIFIER)
    }

    object Attribute : (IElementType?) -> TextAttributesKey? {
        override fun invoke(type: IElementType?): TextAttributesKey? = when (type.antlrType) {
            StitcherLexer.COND_MARK -> AttributeKeys.COND_MARKER
            StitcherLexer.SWAP_MARK -> AttributeKeys.SWAP_MARKER
            StitcherLexer.REPL_MARK -> AttributeKeys.REPL_MARKER

            StitcherLexer.COMMON_COMP,
            StitcherLexer.SEMVER_COMP,
            StitcherLexer.OP_NOT,
            StitcherLexer.OP_AND,
            StitcherLexer.OP_OR,
            StitcherLexer.OP_ASSIGN -> AttributeKeys.OPERATOR

            StitcherLexer.NUMERIC,
            StitcherLexer.DOT,
            StitcherLexer.DASH,
            StitcherLexer.PLUS -> AttributeKeys.VERSION

            StitcherLexer.SUGAR_IF,
            StitcherLexer.SUGAR_ELIF,
            StitcherLexer.SUGAR_ELSE -> AttributeKeys.KEYWORD

            StitcherLexer.LEFT_BRACE,
            StitcherLexer.RIGHT_BRACE,
            StitcherLexer.SCOPE_OPEN,
            StitcherLexer.SCOPE_WORD,
            StitcherLexer.SCOPE_CLOSE -> AttributeKeys.BRACES

            StitcherLexer.IDENTIFIER -> AttributeKeys.IDENTIFIER
            StitcherLexer.QUOTED -> AttributeKeys.LITERAL
            StitcherLexer.COMMENT -> AttributeKeys.COMMENT

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

    override fun getHighlightingLexer(): Lexer = ANTLRLexerAdaptor(StitcherLang, StitcherLexer(null))

    override fun getTokenHighlights(type: IElementType?): Array<out TextAttributesKey> =
        Attribute(type)?.let { arrayOf(it) } ?: emptyArray()
}