package dev.kikugie.stonecutter.intellij.lang.parsing

import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherType

interface AstBuilder {
    val text: String?
    val current: IElementType?

    fun advance()
    fun reassign(type: IElementType)
    fun report(message: String)
    fun peek(steps: Int): IElementType?
    fun wrap(type: StitcherType, builder: (Cancellable) -> Unit)

    fun interface Cancellable {
        fun cancel()
    }
}