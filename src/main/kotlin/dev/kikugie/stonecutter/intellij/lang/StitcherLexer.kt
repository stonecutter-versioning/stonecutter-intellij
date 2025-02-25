package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerBase
import com.intellij.lexer.MergeFunction
import com.intellij.lexer.MergingLexerAdapterBase
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypes
import com.intellij.psi.tree.IElementType
import dev.kikugie.stitcher.data.token.MarkerType
import dev.kikugie.stitcher.lexer.LexSlice
import dev.kikugie.stitcher.lexer.TokenMatcher
import dev.kikugie.stonecutter.intellij.lang.token.convert

class StitcherLexer : MergingLexerAdapterBase(LexerImpl()) {
    override fun getMergeFunction(): MergeFunction? = Merger

    class LexerImpl : LexerBase() {
        private lateinit var sequence: CharSequence
        private lateinit var matcher: TokenMatcher
        private var current: LexSlice? = null
        private var cursor = 0

        override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
            sequence = buffer
            matcher = TokenMatcher(buffer)
            cursor = startOffset
            current = if (startOffset != 0) next() else when(buffer.firstOrNull()) {
                '?' -> LexSlice(MarkerType.CONDITION, 0, buffer).also { cursor++ }
                '$' -> LexSlice(MarkerType.SWAP, 0, buffer).also { cursor++ }
                else -> next()
            }
        }

        override fun getState(): Int = 0
        override fun getBufferSequence(): CharSequence = sequence
        override fun getBufferEnd(): Int = sequence.length
        override fun getTokenType(): IElementType? = current?.type?.convert()
        override fun getTokenStart(): Int = current?.range?.start ?: sequence.length
        override fun getTokenEnd(): Int = current?.range?.run { last + 1 } ?: sequence.length

        override fun advance() {
            current = next()
        }

        private fun next(): LexSlice? {
            if (cursor >= sequence.length) return null
            return matcher.match(cursor).also { cursor = it.range.last + 1 }
        }
    }

    object Merger : MergeFunction {
        override fun merge(type: IElementType, original: Lexer): IElementType? {
            return null
        }
    }
}