package dev.kikugie.stonecutter.intellij.impl

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.EditorHighlighterProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.CONDITION_MARKER
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.ELIF
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.ELSE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.EXPECT_WORD
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.GROUP_CLOSE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.GROUP_OPEN
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.IDENTIFIER
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.IF
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.PREDICATE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SCOPE_CLOSE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SCOPE_OPEN
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SWAP_MARKER
import javax.swing.Icon

class StitcherHighlighter : SyntaxHighlighterBase(), DumbAware {
    override fun getHighlightingLexer(): Lexer = StitcherLexer()

    override fun getTokenHighlights(type: IElementType?): Array<TextAttributesKey> = when (type) {
        IF, ELSE, ELIF -> Constants.SUGAR_KEY
        IDENTIFIER -> Constants.IDENTIFIER_KEY
        PREDICATE -> Constants.PREDICATE_KEY
        CONDITION_MARKER, SWAP_MARKER -> Constants.MARKER_KEY
        GROUP_OPEN, GROUP_CLOSE -> DefaultLanguageHighlighterColors.BRACES
        SCOPE_OPEN, SCOPE_CLOSE -> DefaultLanguageHighlighterColors.BRACKETS
        EXPECT_WORD -> DefaultLanguageHighlighterColors.LABEL
        else -> null
    }.let(SyntaxHighlighterBase::pack)

    object Constants {
        val SUGAR_KEY = createTextAttributesKey("STITCHER_SUGAR", DefaultLanguageHighlighterColors.KEYWORD)
        val IDENTIFIER_KEY = createTextAttributesKey("STITCHER_IDENTIFIER", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
        val PREDICATE_KEY = createTextAttributesKey("STITCHER_PREDICATE", DefaultLanguageHighlighterColors.NUMBER)
        val MARKER_KEY = createTextAttributesKey("STITCHER_MARKER", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
    }
}

class StitcherEditorHighlighter : EditorHighlighterProvider, DumbAware {
    override fun getEditorHighlighter(
        project: Project?,
        fileType: FileType,
        virtualFile: VirtualFile?,
        colors: EditorColorsScheme,
    ): EditorHighlighter = LexerEditorHighlighter(StitcherHighlighter(), colors)
}

class StitcherAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {

    }
}

class StitcherColorSettingsPage : ColorSettingsPage {
    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = Constants.DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Stitcher"

    override fun getIcon(): Icon? = null

    override fun getHighlighter(): SyntaxHighlighter = StitcherHighlighter()

    override fun getDemoText(): String = """
        //? if condition {
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): MutableMap<String, TextAttributesKey>? = null

    object Constants {
        val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Condition sugar", StitcherHighlighter.Constants.SUGAR_KEY),
            AttributesDescriptor("Type marker", StitcherHighlighter.Constants.MARKER_KEY),
            AttributesDescriptor("Identifier", StitcherHighlighter.Constants.IDENTIFIER_KEY),
            AttributesDescriptor("Predicate", StitcherHighlighter.Constants.PREDICATE_KEY)
        )
    }
}