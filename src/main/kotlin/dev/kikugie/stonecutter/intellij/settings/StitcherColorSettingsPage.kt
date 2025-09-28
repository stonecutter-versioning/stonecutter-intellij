package dev.kikugie.stonecutter.intellij.settings

import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.java.JavaSyntaxHighlighterFactory
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys

class StitcherColorSettingsPage : ColorSettingsPage {
    private val descriptors = arrayOf(
        AttributesDescriptor("Markers", AttributeKeys.MARKER),
        AttributesDescriptor("Markers//Condition (?)", AttributeKeys.COND_MARKER),
        AttributesDescriptor("Markers//Swap ($)", AttributeKeys.SWAP_MARKER),
        AttributesDescriptor("Markers//Replacement (~)", AttributeKeys.REPL_MARKER),
        AttributesDescriptor("Keywords (if, else, elif)", AttributeKeys.KEYWORD),
        AttributesDescriptor("Operators (>=, <=, ==, ^, &&, ||)", AttributeKeys.OPERATOR),
        AttributesDescriptor("Versions", AttributeKeys.VERSION),
        AttributesDescriptor("Braces { }", AttributeKeys.BRACES),
        AttributesDescriptor("Identifiers", AttributeKeys.IDENTIFIER),
        AttributesDescriptor("Identifiers//Constant", AttributeKeys.CONSTANT),
        AttributesDescriptor("Identifiers//Swap", AttributeKeys.SWAP),
        AttributesDescriptor("Identifiers//Dependency", AttributeKeys.DEPENDENCY),
        AttributesDescriptor("Identifiers//Replacement", AttributeKeys.REPLACEMENT),
    )

    override fun getDisplayName() = "Stonecutter"
    override fun getIcon() = null
    override fun getAttributeDescriptors() = descriptors
    override fun getColorDescriptors() = ColorDescriptor.EMPTY_ARRAY
    override fun getHighlighter() = JavaSyntaxHighlighterFactory.getSyntaxHighlighter(JavaLanguage.INSTANCE, null, null)

    override fun getDemoText() = """
        public class ExampleMod implements ModInitializer {
            public static final Logger LOGGER = LoggerFactory.getLogger("example");
            
            // Swap marker example - version string replacement
            public static final String VERSION = /*<marker>$</marker> <dep>mod_version</dep>*/ "1.0.0";

            @Override
            public void onInitialize() {
                // Basic conditional with version check
                //<marker>?</marker> <keyword>if</keyword> <op>>=</op><num>1.21</num> <braces>{</braces>
                /*LOGGER.info("We have trial chambers!");
                LOGGER.info("Let's put our maces to their faces!");
                *///<marker>?</marker><braces>}</braces>
                
                // Condition branching with else if
                //<marker>?</marker> <keyword>if</keyword> <op>=</op><num>1.20.1</num> <braces>{</braces>
                LOGGER.info("Trails and Tales update just released!");
                //<marker>?</marker><braces>}</braces> <keyword>elif</keyword> <op>=</op><num>1.21.1</num> <braces>{</braces>
                /*LOGGER.info("Tricky Trials update just released!");
                *///<marker>?</marker><braces>}</braces> <keyword>else</keyword> <keyword>if</keyword> <op>=</op><num>1.21.4</num> <braces>{</braces>
                /*LOGGER.info("The Garden Awakens drop just dropped...");
                *///<marker>?</marker><braces>}</braces>
                
                // Dependency checking
                //<marker>?</marker> <keyword>if</keyword> <dep>fabric_api</dep><op>:</op> <op>>=</op><num>0.95.0</num> <braces>{</braces>
                LOGGER.info("Fabric API is up to date!");
                //<marker>?</marker><braces>}</braces> <keyword>else</keyword> <braces>{</braces>
                /*LOGGER.info("Please update Fabric API!");
                *///<marker>?</marker><braces>}</braces>
                
                // Inline conditional comments
                MinecraftClass.method(/*<marker>?</marker> <op><=</op><num>1.21.1</num> <braces>{</braces>*/ null /*<braces>}</braces> <keyword>else</keyword> <braces>{</braces>*//* <num>1.0</num> *//*<marker>?</marker><braces>}</braces>*/);
                
                // Line scope without braces
                //<marker>?</marker> <keyword>if</keyword> <op><</op><num>1.21</num>
                LOGGER.info("This version is so old!");
                
                // Nested conditions with complex operators
                //<marker>?</marker> <keyword>if</keyword> <op>>=</op><num>1.20</num> <op>&&</op> <op><</op><num>1.21</num> <braces>{</braces>
                registerOldFeatures();
                //<marker>?</marker><braces>}</braces>
            }
            
            private void registerOldFeatures() {
                // Method implementation
            }
        }
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
        return mapOf(
            "marker" to AttributeKeys.MARKER,
            "keyword" to AttributeKeys.KEYWORD,
            "op" to AttributeKeys.OPERATOR,
            "num" to AttributeKeys.VERSION,
            "id" to AttributeKeys.IDENTIFIER,
            "braces" to AttributeKeys.BRACES,
            "const" to AttributeKeys.CONSTANT,
            "dep" to AttributeKeys.DEPENDENCY,
            "repl" to AttributeKeys.REPLACEMENT,
            "swap" to AttributeKeys.SWAP,
        )
    }
}