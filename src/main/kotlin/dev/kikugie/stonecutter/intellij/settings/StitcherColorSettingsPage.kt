package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter
import dev.kikugie.stonecutter.intellij.editor.StitcherTextAttributesKeys

class StitcherColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Markers", StitcherTextAttributesKeys.STITCHER_MARKER),
        AttributesDescriptor("Keywords", StitcherTextAttributesKeys.STITCHER_KEYWORD),
        AttributesDescriptor("Operators", StitcherTextAttributesKeys.STITCHER_OPERATOR),
        AttributesDescriptor("Numbers", StitcherTextAttributesKeys.STITCHER_NUMBER),
        AttributesDescriptor("Identifiers", StitcherTextAttributesKeys.STITCHER_IDENTIFIER),
        AttributesDescriptor("Braces", StitcherTextAttributesKeys.STITCHER_BRACES),
        AttributesDescriptor("Constants", StitcherTextAttributesKeys.STITCHER_CONSTANT),
        AttributesDescriptor("Dependencies", StitcherTextAttributesKeys.STITCHER_DEPENDENCY)
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