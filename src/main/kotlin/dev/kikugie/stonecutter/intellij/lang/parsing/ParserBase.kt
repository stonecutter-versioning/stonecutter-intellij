package dev.kikugie.stonecutter.intellij.lang.parsing

import com.intellij.lang.PsiBuilder
import com.intellij.lang.SyntaxTreeBuilder
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType

abstract class ParserBase(val builder: PsiBuilder) {
    internal val text: String? get() = builder.tokenText
    internal val current: IElementType? get() = builder.tokenType

    internal fun advance() = builder.advanceLexer()
    internal fun advance(n: Int) = repeat(n) { advance() }

    internal fun reassign(type: IElementType) = builder.remapCurrentToken(type)

    internal fun peek(steps: Int = 1): IElementType? = builder.lookAhead(steps)

    internal fun report(message: String, start: Boolean = false) {
        if (current == null || start) builder.error(message)
        else builder.mark().error(message)
    }

    internal inline fun <T> consuming(block: (StitcherType) -> T): T =
        block(current as StitcherType).also { advance() }

    internal inline fun <T> advancing(block: () -> T): T =
        (current as StitcherType).let { advance(); block() }

    internal inline fun consumeIfAny(message: String, condition: (IElementType) -> Boolean = { true }) {
        if (current.let { it == null || !condition(it) }) return
        else consumeWhile(message, condition)
    }

    internal inline fun consumeWhile(message: String, condition: (IElementType) -> Boolean = { true }) {
        if (current == null) return builder.error(message)
        val mark = builder.mark()
        while (current.let { it != null && condition(it) })
            advance()
        mark.error(message)
    }

    internal fun wrap(type: StitcherType) {
        val mark = builder.mark()
        advance()
        mark.done(type)
    }

    internal inline fun <T> wrap(type: StitcherType, action: SyntaxTreeBuilder.Marker.() -> T): T? = with(builder.mark()) {
        var result: T? = null
        try {
            result = action()
            done(type)
        } catch (e: Throwable) {
            if (e is MarkerQuitException) drop()
            else error(e.message ?: e.stackTraceToString())
        }
        result
    }

    internal object MarkerQuitException : RuntimeException() {
        private fun readResolve(): Any = MarkerQuitException
    }
}