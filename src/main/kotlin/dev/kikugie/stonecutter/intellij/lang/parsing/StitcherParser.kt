package dev.kikugie.stonecutter.intellij.lang.parsing

import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherConditionSugarType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherMarkerType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherPrimitiveType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherScopeType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherTypes
import dev.kikugie.stonecutter.intellij.util.whenIt
import dev.kikugie.stonecutter.intellij.util.whenNot

class StitcherParser(val builder: AstBuilder, val errors: ErrorHandlers) {
    private object Errors {
        const val UNRECOGNIZED = "UNRECOGNIZED"
        const val EXPRESSION = "CONDITION_EXPRESSSION"
        const val SUGAR = "CONDITION_SUGAR"
    }

    private object Scopes {
        const val SUGAR = "SUGAR_SCOPE"
    }

    fun parse() = builder.wrap(StitcherTypes.Component.DEFINITION) {
        val marker = consuming {
            check(it is StitcherMarkerType) { "Invalid marker '${builder.text}'" }; it
        }

        val extension = when (builder.current) {
            StitcherTypes.Scope.CLOSE -> consuming { true }
            null -> error("Empty comment body")
            else -> false
        }

        when (marker) {
            StitcherTypes.Marker.SWAP -> parseSwap(extension)
            StitcherTypes.Marker.CONDITION -> parseCondition(extension)
        }

        if (builder.current.isCloser) builder.advance()
    }


    fun parseSwap(extension: Boolean) = builder.wrap(StitcherTypes.Component.SWAP) {
        if (extension && builder.current != null) builder.wrap(StitcherTypes.Invalid) {
            while (builder.current != null) builder.advance()
            error("Swap closers must be empty")
        }

        var identifier = false
        while (!builder.current.isCloser) when (builder.current) {
            StitcherTypes.Primitive.IDENTIFIER -> consuming {
                if (!identifier && Errors.UNRECOGNIZED !in errors) identifier =
                    true.also { builder.reassign(StitcherTypes.Primitive.SWAP) }
                else errors.handle(Errors.UNRECOGNIZED, msg = "Unexpected expression")
            }

            else -> errors.handle(Errors.UNRECOGNIZED, msg = "Unexpected expression")
        }
        errors.releaseAll()
        check(identifier) { "Missing identifier" }
    }

    fun parseCondition(extension: Boolean) = builder.wrap(StitcherTypes.Component.CONDITION) {
        var needsCondition = false
        var hasCondition = false
        val sugar = mutableListOf<StitcherType>()

        while (!builder.current.isCloser) when (builder.current) {
            is StitcherConditionSugarType -> consuming {
                errors.release(Errors.EXPRESSION)
                if (Errors.UNRECOGNIZED in errors) return@consuming
                if (hasCondition) errors.handle(Errors.SUGAR, msg = "Unexpected condition sugar")
                else {
                    errors.handle(Scopes.SUGAR, type = StitcherTypes.Component.SUGAR)
                    validateSugar(extension, sugar).whenIt { needsCondition = true }
                    sugar += it
                }
            }

            is StitcherPrimitiveType,
            StitcherTypes.Operator.NOT,
            StitcherTypes.Operator.LPAREN -> {
                errors.release(Errors.SUGAR, Scopes.SUGAR)
                if (Errors.UNRECOGNIZED in errors) {
                    builder.advance(); continue
                }
                if (hasCondition) consuming { errors.handle(Errors.EXPRESSION, msg = "Unexpected condition expression") }
                else builder.wrap(StitcherTypes.Component.EXPRESSION) { matchExpression(); hasCondition = true }
            }

            else -> consuming {
                errors.release(Errors.SUGAR, Errors.EXPRESSION, Scopes.SUGAR)
                errors.handle(Errors.UNRECOGNIZED, msg = "Unexprected expression")
            }

        }
        errors.releaseAll()
        check(!needsCondition || hasCondition) { "Missing condition expression" }
    }

    fun validateSugar(extension: Boolean, sugar: List<StitcherType>) = when (sugar.firstOrNull()) {
        null -> when (builder.current) {
            StitcherTypes.Sugar.IF -> true.also { if (extension) error("Expected 'else' or 'elif' to follow the extension") }
            StitcherTypes.Sugar.ELSE,
            StitcherTypes.Sugar.ELIF -> (builder.current != StitcherTypes.Sugar.ELSE).also { if (!extension) error("Expected to follow '}' to extend the condition") }

            else -> false.also { error("Unexpected token") }
        }

        StitcherTypes.Sugar.IF,
        StitcherTypes.Sugar.ELIF -> true.also { error("No more condition sugar allowed") }

        StitcherTypes.Sugar.ELSE -> (builder.current == StitcherTypes.Sugar.IF).whenNot { error("Unexpected token") }
        else -> false.also { error("Unexpected token") }
    }

    fun matchExpression(): Unit = when (builder.current) {
        StitcherTypes.Operator.NOT -> maybeMatchBoolean(StitcherTypes.Component.UNARY) {
            advancing { matchExpression() }
        }

        StitcherTypes.Operator.LPAREN -> maybeMatchBoolean(StitcherTypes.Component.GROUP) {
            builder.advance()
            matchExpression()
            check(builder.current != StitcherTypes.Operator.RPAREN) { "Missing group closer" }
            builder.advance()
        }

        StitcherTypes.Primitive.PREDICATE -> maybeMatchBoolean(StitcherTypes.Component.PREDICATE) {
            collectPredicates()
        }

        StitcherTypes.Primitive.IDENTIFIER -> maybeMatchBoolean {
            if (builder.peek(1) != StitcherTypes.Operator.ASSIGN)
                consuming { builder.reassign(StitcherTypes.Primitive.CONSTANT) }
            else builder.wrap(StitcherTypes.Component.ASSIGNMENT) {
                consuming { builder.reassign(StitcherTypes.Primitive.DEPENDENCY) }
                builder.advance()
                if (builder.current == StitcherTypes.Primitive.PREDICATE) builder.wrap(StitcherTypes.Component.PREDICATE) {
                    collectPredicates()
                } else error("No predicate after assignment")
            }
        }

        null, is StitcherScopeType -> builder.report("Incomplete expression")
        else -> builder.report("Unexpected token")
    }

    fun collectPredicates() {
        while (true) if (builder.current != StitcherTypes.Primitive.PREDICATE) break
        else builder.advance()
    }

    private inline fun maybeMatchBoolean(type: StitcherType? = null, crossinline action: () -> Unit) = builder.wrap(StitcherTypes.Component.BOOLEAN) {
        if (type == null) action()
        else builder.wrap(type) { action() }
        when (builder.current) {
            StitcherTypes.Operator.AND,
            StitcherTypes.Operator.OR -> advancing {
                check(!builder.current.isCloser) { "Incomplete expression" }
                matchExpression()
            }

            else -> it.cancel()
        }
    }

    private val IElementType?.isCloser
        get() = this == null
                || this == StitcherTypes.Scope.OPEN
                || this == StitcherTypes.Scope.WORD

    private inline fun <T> consuming(block: (StitcherType) -> T): T =
        block(builder.current as StitcherType).also { builder.advance() }

    private inline fun <T> advancing(block: () -> T): T =
        (builder.current as StitcherType).let { builder.advance(); block() }
}