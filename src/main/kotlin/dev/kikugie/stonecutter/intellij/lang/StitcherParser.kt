package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiBuilder.Marker
import com.intellij.lang.PsiParser
import com.intellij.openapi.util.Key
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.component.*
import dev.kikugie.stitcher.data.scope.ScopeType
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.data.token.StitcherTokenType.*
import dev.kikugie.stitcher.data.token.Token
import dev.kikugie.stitcher.data.token.TokenType
import dev.kikugie.stitcher.eval.isEmpty
import dev.kikugie.stitcher.eval.isNotEmpty
import dev.kikugie.stonecutter.intellij.util.also
import dev.kikugie.stonecutter.intellij.util.whenIt
import dev.kikugie.stonecutter.intellij.util.whenNot
import kotlin.math.E

class StitcherParser : PsiParser, LightPsiParser {
    companion object {
        val DEFINITION: Key<Definition> = Key("STITCHER_DEFINITION")
    }

    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val definition = parseInternal(builder)
        return builder.treeBuilt.also {
            it.putCopyableUserData(DEFINITION, definition)
        }
    }

    override fun parseLight(root: IElementType, builder: PsiBuilder) {
        parseInternal(builder)
    }

    private fun parseInternal(builder: PsiBuilder): Definition? {
        val mark = builder.mark()
        val definition = StitcherPsiParser(builder).parse()
        if (!builder.eof()) {
            val marker = builder.mark()
            while (!builder.eof()) builder.advanceLexer()
            marker.error("Expected comment to end")
        }
        mark.done(StitcherType.STITCHER_EXPRESSION)
        return definition
    }
}

private class Mark(private val init: () -> Marker) {
    private var marker: Marker? = null
    private lateinit var resetter: Marker.() -> Unit
    fun mark(resetter: Marker.() -> Unit) {
        if (marker == null) {
            marker = init()
            this.resetter = resetter
        }
    }
    fun release() {
        marker?.resetter()
        marker = null
    }
}

private class StitcherPsiParser(builder: PsiBuilder) : PsiBuilder by builder {
    val markers: MutableMap<String, Mark> = mutableMapOf()

    fun parse(): Definition? {
        val type = currentType as? MarkerType ?: return null.also { error("Invalid marker type") }
        advanceLexer()

        val extension = when (currentType) {
            SCOPE_CLOSE -> consuming { true }
            null -> return null.also { error("Empty comment body") }
            else -> false
        }

        val component = when (type) {
            MarkerType.CONDITION -> parseCondition(extension)
            MarkerType.SWAP -> parseSwap(extension)
        }

        val closer = when(currentType) {
            SCOPE_OPEN -> consuming { ScopeType.CLOSED }
            EXPECT_WORD -> consuming { ScopeType.WORD }
            null -> ScopeType.LINE
            else -> consuming { error("Unexpected token"); ScopeType.LINE }
        }
        return Definition(component, extension, closer)
    }

    private fun parseSwap(extension: Boolean): Swap {
        if (extension && tokenType != null) return Swap(Token.EMPTY).also {
            error("Swap closers must be empty")
        }
        val marker = mark()
        var identifier = Token.EMPTY
        while (true) when (currentType) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IDENTIFIER -> consuming {
                if (identifier.isEmpty()) identifier = token()
                else mark("identifiers") { error("Unexpected identifier") }
            }

            else -> consuming {
                releaseExcept("unrecognized")
                mark("unrecognized") { error("Unexpected expression") }
            }
        }
        releaseAll()
        if (identifier.isEmpty()) error("Missing swap identifier")
        marker.done(StitcherType.SWAP)
        return Swap(identifier)
    }

    private fun parseCondition(extension: Boolean): Condition {
        var needsCondition = false
        var expression: Component = Empty
        val sugar = mutableListOf<Token>()
        val marker = mark()

        while (true) when (currentType) {
            SCOPE_OPEN, EXPECT_WORD, null -> break
            IF, ELSE, ELIF -> consuming { token ->
                releaseExcept("sugar")
                if (expression.isNotEmpty()) mark("sugar") { error("Unexpected condition sugar") }
                else {
                    validateCondition(extension, sugar).whenIt { needsCondition = it }
                    sugar += token
                }
            }

            IDENTIFIER, PREDICATE, NEGATE, GROUP_OPEN -> {
                releaseExcept("condition")
                if (!expression.isEmpty()) consuming { mark("condition") { error("Unexpected condition expression") } }
                else expression = parseExpression()
            }

            else -> consuming {
                releaseExcept("unrecognized")
                mark("unrecognized") { error("Unknown expression") }
            }
        }

        releaseAll()
        if ((needsCondition || currentType == SCOPE_CLOSE || currentType == EXPECT_WORD) && expression.isEmpty())
            error("Missing condition expression")
        marker.done(StitcherType.CONDITION)
        return Condition(sugar, expression)
    }

    private fun validateCondition(extension: Boolean, sugar: List<Token>): Boolean =
        when (sugar.firstOrNull()?.type) { // True when must have a condition
            null -> when (currentType) { // The current token is the first sugar
                IF -> true.also { if (extension) error("Expected 'else' or 'elif' to follow the extension") }
                ELSE, ELIF -> (currentType == ASSIGN).also { if (!extension) error("Expected to follow '}' to extend the condition") }
                else -> false.also { error("Unexpected token") }
            }

            IF, ELIF -> true.also { error("No more condition sugar allowed") }
            ELSE -> (currentType == ASSIGN).whenNot { error("Unexpected token") }
            else -> false.also { error("Unexpected token") }
        }

    private fun parseExpression(): Component = when (currentType) {
        NEGATE -> advancing { parseMaybeBoolean(Unary(it, parseExpression())) }
        PREDICATE -> parseMaybeBoolean(Assignment(Token.EMPTY, parsePredicate()))
        IDENTIFIER -> advancing { token ->
            fun IElementType?.isCandidate() = this?.convert()?.let { it == IDENTIFIER || it == PREDICATE } == true
            val component = if (currentType == ASSIGN)
                (if (lookAhead(1)?.isCandidate() == true) advancing { parsePredicate() }
                else consuming { error("No predicate assigned"); emptyList() }).let {
                    Assignment(token, it)
                }
            else if (tokenType?.isCandidate() == true)
                Assignment(Token.EMPTY, listOf(token) + parsePredicate())
            else Literal(token)
            parseMaybeBoolean(component)
        }
        GROUP_OPEN -> advancing {
            val group = Group(parseExpression())
            if (currentType == GROUP_CLOSE) advanceLexer()
            else error("Missing closing bracket")
            parseMaybeBoolean(group)
        }
        SCOPE_OPEN, EXPECT_WORD, null -> Empty.also { error("Incomplete expression") }
        else -> consuming { error("Unexpected token"); Empty }
    }

    private fun parsePredicate(): List<Token> = buildList {
        while (true) when (currentType) { // TODO: Validate if identifier is allowed in the check
            PREDICATE -> consuming { add(it) }
            IDENTIFIER -> consuming { remapCurrentToken(PREDICATE.convert()); add(it) }
            else -> break
        }
    }

    private fun parseMaybeBoolean(left: Component): Component = when (currentType) {
        OR, AND -> {
            val operator = token()
            val right = if (lookAhead(1) != null) advancing { parseExpression() }
            else advancing { error("Incomplete expression"); Empty }
            Binary(left, operator, right)
        }

        else -> left
    }

    private val currentType: TokenType? get() = tokenType?.convert()

    private fun mark(name: String, resetter: Marker.() -> Unit) {
        markers.getOrPut(name) { Mark(::mark) }.mark(resetter)
    }
    private fun release(name: String) {
        markers.remove(name)?.release()
    }
    private fun releaseExcept(vararg names: String) {
        markers.keys.forEach { if (it !in names) release(it) }
    }
    private fun releaseAll() {
        markers.keys.forEach { release(it) }
    }

    private fun token() = Token(tokenText!!, tokenType!!.convert())

    /**Creates a [Token] for the current position, performs the [action] on it and advances the lexer.*/
    private inline fun <T> consuming(action: (Token) -> T): T = action(token()).also { advanceLexer() }

    /**Creates a [Token] for the current position, advances the lexer and performs the [action] on it.*/
    private inline fun <T> advancing(action: (Token) -> T): T = token().let { advanceLexer(); action(it) }
}