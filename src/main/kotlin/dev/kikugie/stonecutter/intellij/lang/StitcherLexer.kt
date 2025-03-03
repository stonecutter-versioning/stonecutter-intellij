package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.TokenMatcher
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType

class StitcherLexer : LexerBase() {
    private lateinit var sequence: CharSequence
    private lateinit var matcher: TokenMatcher
    private var current: LexSlice? = null
    private var cursor = 0

    override fun start(buffer: CharSequence, start: Int, end: Int, state: Int) {
        matcher = TokenMatcher(buffer)
        sequence = buffer
        cursor = start
        current = null
        advance()
    }

    override fun getState(): Int = 0
    override fun getBufferSequence(): CharSequence = sequence
    override fun getBufferEnd(): Int = sequence.length
    override fun getTokenType(): IElementType? = current?.type?.let(StitcherType::convert)
    override fun getTokenStart(): Int = current?.range?.start ?: sequence.length
    override fun getTokenEnd(): Int = current?.range?.run { last + 1 } ?: sequence.length
    override fun advance() {
        current = if (cursor != 0 || !sequence.isNotEmpty()) next()
        else when (sequence.first()) {
            '?' -> LexSlice(MarkerType.CONDITION, 0, sequence).also { cursor++ }
            '$' -> LexSlice(MarkerType.SWAP, 0, sequence).also { cursor++ }
            else -> next()
        }
    }

    private fun next(): LexSlice? {
        if (cursor >= sequence.length) return null
        return matcher.match(cursor).also { cursor = it.range.last + 1 }
    }
}
