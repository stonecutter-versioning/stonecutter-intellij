package dev.kikugie.stonecutter.intellij.lang.parsing

import com.intellij.lang.ASTNode
import com.intellij.lang.LightPsiParser
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Reference
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Component
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Marker
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Operator
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Primitive
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Scope
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType.Sugar
import dev.kikugie.stonecutter.intellij.util.then

class StitcherParser(builder: PsiBuilder) : ParserBase(builder) {
    class Factory : PsiParser, LightPsiParser {
        override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
            builder.setDebugMode(true)
            parseLight(root, builder)
            return builder.treeBuilt
        }

        override fun parseLight(root: IElementType, builder: PsiBuilder) {
            val mark = builder.mark()
            StitcherParser(builder).parse()
            mark.done(root)
        }
    }

    private val IElementType?.isCloser
        get() = when (this) {
            null, Scope.OPEN, Scope.WORD -> true
            else -> false
        }


    fun parse() {
        parseContent()
        consumeIfAny("Unexpected token")
    }

    private fun parseContent() = wrap(Component.DEFINITION) {
        val marker = consuming {
            check(it is Marker) { report("Invalid marker type") }; it
        }

        val isExtension = when (current) {
            Scope.CLOSE -> advance() then true
            null -> throw MarkerQuitException.also { report("Empty contents", true) }
            else -> false
        }

        when (marker) {
            Marker.SWAP -> parseSwap(isExtension)
            Marker.CONDITION -> parseCondition(isExtension)
        }

        if (current.isCloser)
            advance()
    }

    private fun parseSwap(isExtension: Boolean) = wrap(Component.SWAP) {
        if (isExtension && current != null) consumeWhile("Swap closers must be empty")
        fun chewRemaining() = consumeIfAny("Unexpected expression") { !it.isCloser }

        if (current == Primitive.IDENTIFIER) wrap(Reference.SWAP)
        else report("Missing swap identifier", true) then chewRemaining()
        if (!current.isCloser) chewRemaining()
    }

    private fun parseCondition(isExtension: Boolean) = wrap(Component.CONDITION) {
        val needsExpression = parseSugar(isExtension)
        if (!current.isCloser) wrap(Component.EXPRESSION) { parseExpression() }
        else if (needsExpression) report("Missing condition expression", true)
    }

    private fun parseSugar(isExtension: Boolean): Boolean = wrap(Component.SUGAR) {
        fun chewRemaining() = consumeIfAny("Unexpected condition sugar") { it is Sugar }

        when (current) { // True if condition is needed
            Sugar.IF -> {
                if (isExtension) report("IF can only be used in the opening condition")
                advance(); chewRemaining(); true
            }

            Sugar.ELIF -> {
                if (!isExtension) report("ELIF can only be used in extension conditions")
                advance(); chewRemaining(); true
            }

            Sugar.ELSE -> {
                if (!isExtension) report("ELSE can only be used in extension conditions")
                val state = if (peek() == Sugar.IF) advance() then true else false
                advance(); chewRemaining(); state
            }

            else -> false
        }
    } ?: false

    private fun parseExpression(): Unit? = when (current) {
        Primitive.PREDICATE -> maybeMatchBoolean {
            collectPredicates()
        }

        Primitive.IDENTIFIER -> maybeMatchBoolean {
            when (peek()) {
                Operator.ASSIGN -> wrap(Component.ASSIGNMENT) {
                    wrap(Reference.DEPENDENCY); advance()
                    if (current != Primitive.PREDICATE) error("No predicate after assignment")
                    else collectPredicates()
                }

                Operator.OR, Operator.AND -> wrap(Reference.CONSTANT)
                else -> wrap(Reference.AMBIGUOUS)
            }
        }

        Operator.NOT -> maybeMatchBoolean {
            wrap(Component.UNARY) {
                advance(); parseExpression()
            }
        }

        Operator.LPAREN -> maybeMatchBoolean {
            wrap(Component.GROUP) {
                advance(); parseExpression()
                if (current == Operator.RPAREN) advance()
                else report("Missing closing parenthesis", true)
            }
        }

        null, Scope.OPEN, Scope.WORD -> report("Incomplete expression", true)
        else -> consumeWhile("Unexpected expression") { !it.isCloser }
    }

    fun collectPredicates() = wrap(Component.PREDICATE) {
        while (true) if (current != Primitive.PREDICATE) break
        else advance()
    }

    private inline fun maybeMatchBoolean(crossinline action: () -> Unit) = wrap(Component.BINARY) {
        action() then when (current) {
            Operator.OR, Operator.AND -> advancing {
                check(!current.isCloser) { "Incomplete expression" }
                parseExpression()
            }

            else -> throw MarkerQuitException
        }
    }
}