package dev.kikugie.stonecutter.intellij.lang.layout

import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

enum class StitcherBlockType {
    CONTENT, COMMENT, CODE, ROOT;

    fun asIElementType(): BlockIElementType = TYPES[ordinal]

    companion object {
        private val TYPES: List<BlockIElementType> = entries.map(::BlockIElementType)
    }
}

class BlockIElementType internal constructor(val value: StitcherBlockType) : IElementType(value.name, StitcherLang)