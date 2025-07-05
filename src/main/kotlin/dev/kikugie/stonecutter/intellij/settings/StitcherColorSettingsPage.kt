package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys

class StitcherColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Markers//Conditional marker (//?)", AttributeKeys.MARKER),
        AttributesDescriptor("Markers//Swap marker (//$)", AttributeKeys.MARKER),
        AttributesDescriptor("Markers//Replacement marker (//~)", AttributeKeys.MARKER),

        AttributesDescriptor("Control Flow//Keywords (if, else, elif)", AttributeKeys.KEYWORD),

        AttributesDescriptor("Operators//Comparison (>=, <=, ==, ~, ^)", AttributeKeys.OPERATOR),
        AttributesDescriptor("Operators//Logical (&&, ||, !)", AttributeKeys.OPERATOR),
        AttributesDescriptor("Operators//Assignment (:)", AttributeKeys.OPERATOR),

        AttributesDescriptor("Syntax//Numbers", AttributeKeys.NUMBER),
        AttributesDescriptor("Syntax//Identifiers", AttributeKeys.IDENTIFIER),
        AttributesDescriptor("Syntax//Braces ( )", AttributeKeys.BRACES),

        AttributesDescriptor("Semantic//Constants", AttributeKeys.CONSTANT),
        AttributesDescriptor("Semantic//Dependencies", AttributeKeys.DEPENDENCY),
        AttributesDescriptor("Semantic//Replacements", AttributeKeys.REPLACEMENT),
        AttributesDescriptor("Semantic//Swaps", AttributeKeys.SWAP),
    )

    override fun getDisplayName() = "Stonecutter"
    override fun getIcon() = null
    override fun getAttributeDescriptors() = descriptors
    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
    override fun getHighlighter() = StitcherSyntaxHighlighter()

    override fun getDemoText() = """
        public class TemplateMod implements ModInitializer {
            public static final Logger LOGGER = LoggerFactory.getLogger("template");
            public static final String VERSION = /*<marker>$</marker> <dep>mod_version</dep>*/ "0.1.0";

            @Override
            public void onInitialize() {

                //<marker>?</marker> <keyword>if</keyword> <op>>=</op> <num>1.21.5</num> <braces>{</braces>
                /* LOGGER.info("Running >= 1.21.5");
                *///<marker>?</marker><braces>}</braces> <keyword>else</keyword> <keyword>if</keyword> <op>>=</op><num>1.20.4</num> <braces>{</braces>
                /* LOGGER.info("Running >= 1.20.4");
                *///<marker>?</marker><braces>}</braces> <keyword>else</keyword> <braces>{</braces>
                LOGGER.info("Running Other Version");
                //<marker>?</marker><braces>}</braces>



                //<marker>?</marker> <keyword>if</keyword> <dep>bapi</dep><op>:</op> <op><</op><num>0.95</num> <braces>{</braces>
                LOGGER.info("Fabric API is old on this version");
                LOGGER.info("Please update me!");
                //<marker>?</marker><braces>}</braces>


                /*<marker>?</marker> <op>>=</op><num>1.21.5</num> <braces>{</braces>*/ /*Method*//*<marker>?</marker><braces>}</braces> <keyword>else</keyword> <braces>{</braces>*/ long vcar = 1; /*<marker>?</marker><braces>}</braces>
            }
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
        return mapOf(
            "marker" to AttributeKeys.MARKER,
            "keyword" to AttributeKeys.KEYWORD,
            "op" to AttributeKeys.OPERATOR,
            "num" to AttributeKeys.NUMBER,
            "id" to AttributeKeys.IDENTIFIER,
            "braces" to AttributeKeys.BRACES,
            "const" to AttributeKeys.CONSTANT,
            "dep" to AttributeKeys.DEPENDENCY,
            "repl" to AttributeKeys.REPLACEMENT,
            "swap" to AttributeKeys.SWAP,
        )
    }
}