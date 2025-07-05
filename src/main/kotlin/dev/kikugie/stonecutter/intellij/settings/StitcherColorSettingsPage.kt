package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter
import dev.kikugie.stonecutter.intellij.editor.StitcherTextAttributesKeys

class StitcherColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Markers//Conditional marker (//?)", StitcherTextAttributesKeys.STITCHER_MARKER),
        AttributesDescriptor("Markers//Swap marker (//\$)", StitcherTextAttributesKeys.STITCHER_MARKER),
        AttributesDescriptor("Markers//Replacement marker (//~)", StitcherTextAttributesKeys.STITCHER_MARKER),

        AttributesDescriptor("Control Flow//Keywords (if, else, elif)", StitcherTextAttributesKeys.STITCHER_KEYWORD),

        AttributesDescriptor("Operators//Comparison (>=, <=, ==, ~, ^)", StitcherTextAttributesKeys.STITCHER_OPERATOR),
        AttributesDescriptor("Operators//Logical (&&, ||, !)", StitcherTextAttributesKeys.STITCHER_OPERATOR),
        AttributesDescriptor("Operators//Assignment (:)", StitcherTextAttributesKeys.STITCHER_OPERATOR),

        AttributesDescriptor("Syntax//Numbers", StitcherTextAttributesKeys.STITCHER_NUMBER),
        AttributesDescriptor("Syntax//Identifiers", StitcherTextAttributesKeys.STITCHER_IDENTIFIER),
        AttributesDescriptor("Syntax//Braces ( )", StitcherTextAttributesKeys.STITCHER_BRACES),

        AttributesDescriptor("Semantic//Constants", StitcherTextAttributesKeys.STITCHER_CONSTANT),
        AttributesDescriptor("Semantic//Dependencies", StitcherTextAttributesKeys.STITCHER_DEPENDENCY)
    )

    override fun getDisplayName() = "Stonecutter"
    override fun getIcon() = null
    override fun getAttributeDescriptors() = descriptors
    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
    override fun getHighlighter() = StitcherSyntaxHighlighter()

    override fun getDemoText() = """
        <marker>//? if</marker> <dep>fabric</dep> <op>>=</op> <num>1.21.0</num> <braces>{</braces>
        /*this.world.addParticle(
        *<marker>//?}</marker> <keyword>else</keyword> <braces>{</braces>
        this.world.spawnParticle(
        <marker>//?}</marker>
        
        <marker>//? if</marker> <const>MC_VERSION</const> <op>>=</op> <num>1.20</num> <op>&&</op> <dep>minecraft</dep> <op>~</op> <num>1.20</num> <braces>{</braces>
        // New API available
        <marker>//?}</marker>
        
        <marker>//$</marker> <id>modernMethod</id> <op>>></op> <braces>{</braces>
        // Modern implementation
        <marker>//$}</marker> <keyword>else</keyword> <braces>{</braces>
        // Legacy fallback
        <marker>//$}</marker>
        
        <marker>//~</marker> <id>replaceThis</id>
        // Content to be replaced
        <marker>//~</marker>
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
        return mapOf(
            "marker" to StitcherTextAttributesKeys.STITCHER_MARKER,
            "keyword" to StitcherTextAttributesKeys.STITCHER_KEYWORD,
            "op" to StitcherTextAttributesKeys.STITCHER_OPERATOR,
            "num" to StitcherTextAttributesKeys.STITCHER_NUMBER,
            "id" to StitcherTextAttributesKeys.STITCHER_IDENTIFIER,
            "braces" to StitcherTextAttributesKeys.STITCHER_BRACES,
            "const" to StitcherTextAttributesKeys.STITCHER_CONSTANT,
            "dep" to StitcherTextAttributesKeys.STITCHER_DEPENDENCY
        )
    }
}