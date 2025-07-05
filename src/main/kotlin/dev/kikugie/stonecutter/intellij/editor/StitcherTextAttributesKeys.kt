package dev.kikugie.stonecutter.intellij.editor

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object StitcherTextAttributesKeys {
    @JvmField
    val STITCHER_MARKER = createTextAttributesKey("STONECUTTER_MARKER", DefaultLanguageHighlighterColors.KEYWORD)

    @JvmField
    val STITCHER_KEYWORD = createTextAttributesKey("STONECUTTER_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

    @JvmField
    val STITCHER_OPERATOR = createTextAttributesKey("STONECUTTER_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)

    @JvmField
    val STITCHER_NUMBER = createTextAttributesKey("STONECUTTER_NUMBER", DefaultLanguageHighlighterColors.NUMBER)

    @JvmField
    val STITCHER_IDENTIFIER = createTextAttributesKey("STONECUTTER_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)

    @JvmField
    val STITCHER_BRACES = createTextAttributesKey("STONECUTTER_BRACES", DefaultLanguageHighlighterColors.BRACES)

    @JvmField
    val STITCHER_CONSTANT = createTextAttributesKey("STONECUTTER_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)

    @JvmField
    val STITCHER_DEPENDENCY = createTextAttributesKey("STONECUTTER_DEPENDENCY", DefaultLanguageHighlighterColors.STRING)
}