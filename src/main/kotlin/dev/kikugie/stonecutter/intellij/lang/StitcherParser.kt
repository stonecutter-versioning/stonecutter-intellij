package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.PsiParser
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.ASSIGNMENT_ENTRY
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.BOOLEAN_ENTRY
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.CONDITION
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.CONDITION_EXPRESSION
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.CONDITION_SUGAR
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.DEFINITION
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.DEPENDENCY_ID
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.GROUP_ENTRY
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.PREDICATE_ENTRY
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.SWAP
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SWAP_ID
import dev.kikugie.stonecutter.intellij.lang.StitcherComponentType.Companion.UNARY_ENTRY
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.AND
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.ASSIGNMENT
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.CONDITION_MARKER
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.CONSTANT_ID
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.EXPECT_WORD
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.IDENTIFIER
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.IF
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.ELSE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.ELIF
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.GROUP_CLOSE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.GROUP_OPEN
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.NEGATE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.OR
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.PREDICATE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SCOPE_CLOSE
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SCOPE_OPEN
import dev.kikugie.stonecutter.intellij.lang.StitcherTokenType.Companion.SWAP_MARKER
import dev.kikugie.stonecutter.intellij.util.whenIt
import dev.kikugie.stonecutter.intellij.util.whenNot
import java.util.concurrent.ConcurrentHashMap

class StitcherParser : PsiParser, LightPsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        parseLight(root, builder)
        return builder.treeBuilt
    }

    override fun parseLight(root: IElementType, builder: PsiBuilder) = with(builder) {
        withMarker(root) {
            StitcherPsiParser(builder).parse()
            if (tokenType != null) withMarker(TokenType.ERROR_ELEMENT) {
                while (!eof()) advanceLexer()
                throw MarkerException("Expected comment to end")
            }
        }
    }
}

private class Handler(private val init: () -> Marker, private val type: StitcherType?) {
    private var marker: Marker? = null
    private lateinit var msg: String
    fun mark(message: String) {
        if (marker != null) return
        marker = init()
        msg = message
    }

    fun release() = marker?.run {
        if (type == null) error(msg)
        else done(type)
        marker = null
    }
}

private class MarkerException(val error: String) : Throwable()

private class StitcherPsiParser(builder: PsiBuilder) : PsiBuilder by builder {
    val handlers: MutableMap<String, Handler> = ConcurrentHashMap()

    fun parse() = withMarker(DEFINITION) {
        val marker = consuming {
            if (it != CONDITION_MARKER && it != SWAP_MARKER)
                throw err("Invalid marker type '$it'")
            it
        }
        val extension = if (tokenType != null) consuming { it == SCOPE_CLOSE }
        else throw err("Empty comment body")

        when (marker) {
            CONDITION_MARKER -> parseCondition(extension)
            SWAP_MARKER -> parseSwap(extension)
        }

        if (tokenType == SCOPE_OPEN || tokenType == EXPECT_WORD) advanceLexer()
    }

    fun parseSwap(extension: Boolean) = withMarker(SWAP) {
        if (extension && tokenType != null) {
            while (!eof()) advanceLexer()
            throw err("Swap closers must be empty")
        }
        var identifier: StitcherType? = null
        while (true) when (tokenType) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IDENTIFIER -> consuming {
                if (identifier == null && "unrecognized" !in handlers) identifier =
                    SWAP_ID.also { remapCurrentToken(it) }
                else handle("unrecognized", "Unexpected expression")
            }

            else -> handle("unrecognized", "Unexpected expression")
        }
        release()
        if (identifier == null) throw err("Missing identifier")
    }

    fun parseCondition(extension: Boolean) = withMarker(CONDITION) {
        var needsCondition = false
        var hasCondition = false
        val sugar = mutableListOf<StitcherType>()

        while (true) when (tokenType) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IF, ELSE, ELIF -> consuming {
                release("cond_err")
                if ("unrecognized" in handlers) return@consuming
                if (hasCondition) handle("sugar_err", "Unexpected condition sugar")
                else {
                    handle("sugar", CONDITION_SUGAR)
                    validateSugar(extension, sugar).whenIt { bl -> needsCondition = bl }
                    sugar += it
                }
            }

            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN -> {
                release("sugar", "sugar_err")
                if ("unrecognized" in handlers) {
                    advanceLexer(); continue
                }
                if (hasCondition) consuming { handle("cond_err", "Unexpected condition expression") }
                else withMarker(CONDITION_EXPRESSION) { matchExpression(); hasCondition = true }
            }

            else -> {
                release("sugar", "sugar_err", "cond_err")
                handle("unrecognized", "Unexpected expression")
            }
        }
        release()
        if ((needsCondition || tokenType == SCOPE_OPEN || tokenType == EXPECT_WORD) && !hasCondition)
            throw err("Missing condition expression")
    }

    fun validateSugar(extension: Boolean, sugar: List<StitcherType>) = when (sugar.firstOrNull()) {
        null -> when (tokenType) { // The current token is the first sugar
            IF -> true.also { if (extension) error("Expected 'else' or 'elif' to follow the extension") }
            ELSE, ELIF -> (tokenType != ELSE).also { if (!extension) error("Expected to follow '}' to extend the condition") }
            else -> false.also { error("Unexpected token") }
        }

        IF, ELIF -> true.also { error("No more condition sugar allowed") }
        ELSE -> (tokenType == IF).whenNot { error("Unexpected token") }
        else -> false.also { error("Unexpected token") }
    }

    fun matchExpression(): Unit = when (tokenType) {
        NEGATE -> maybeMatchBoolean(UNARY_ENTRY) {
            advanceLexer()
            matchExpression()

        }

        PREDICATE -> maybeMatchBoolean(PREDICATE_ENTRY) {
            collectPredicates()
        }

        IDENTIFIER -> maybeMatchBoolean {
            if (lookAhead(1) as? StitcherType != ASSIGNMENT) consuming { remapCurrentToken(CONSTANT_ID) }
            else withMarker(ASSIGNMENT_ENTRY) {
                consuming { remapCurrentToken(DEPENDENCY_ID) }
                if (tokenType == PREDICATE) withMarker(PREDICATE_ENTRY) { collectPredicates() }
                else throw err("No predicate after assignment")
            }
        }

        GROUP_OPEN -> maybeMatchBoolean(GROUP_ENTRY) {
            advanceLexer()
            matchExpression()
            if (tokenType == GROUP_CLOSE) advanceLexer()
            else throw err("Missing group closer")
        }

        SCOPE_OPEN, EXPECT_WORD, null -> {
            error("Incomplete expression")
        }

        else -> {
            error("Unexpected token")
        }
    }

    fun collectPredicates() {
        while (true) when (tokenType) {
            PREDICATE -> advanceLexer()
            else -> break
        }
    }

    inline fun maybeMatchBoolean(type: StitcherType? = null, action: () -> Unit) {
        val bool = mark()
        if (type == null) action()
        else withMarker(type, action)
        when (tokenType) {
            AND, OR -> advancing {
                when (tokenType) {
                    SCOPE_OPEN, EXPECT_WORD, null -> bool.error("Incomplete expression")
                    else -> matchExpression().also { bool.done(BOOLEAN_ENTRY) }
                }
            }

            else -> bool.drop()
        }
    }

    private fun handle(name: String, message: String): Unit =
        handlers.getOrPut(name) { Handler(::mark, null) }.mark(message)

    @Suppress("SameParameterValue")
    private fun handle(name: String, type: StitcherType): Unit =
        handlers.getOrPut(name) { Handler(::mark, type) }.mark("")

    private fun release(vararg names: String) = names.forEach { handlers.remove(it)?.release() }
    private fun release() = handlers.keys.forEach { release(it) }

    private fun err(error: String) = MarkerException(error)

    private inline fun <T> consuming(action: (StitcherType) -> T): T =
        action(tokenType as StitcherType).also { advanceLexer() }

    private inline fun <T> advancing(action: (StitcherType) -> T): T =
        (tokenType as StitcherType).let { advanceLexer(); action(it) }
}

private inline fun PsiBuilder.withMarker(type: IElementType, action: () -> Unit) = with(mark()) {
    try {
        action()
        done(type)
    } catch (e: MarkerException) {
        error(e.error)
    }
}