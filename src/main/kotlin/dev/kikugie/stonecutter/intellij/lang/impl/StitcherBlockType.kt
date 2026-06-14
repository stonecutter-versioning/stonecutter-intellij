package dev.kikugie.stonecutter.intellij.lang.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.lang.StitcherLang

enum class StitcherBlockType {
    CONTENT, COMMENT, CODE, ROOT;

    fun asIElementType(): BlockIElementType = TYPES[ordinal]

    companion object {
        private val TYPES: List<BlockIElementType> = entries.map(::BlockIElementType)
    }
}

class BlockIElementType internal constructor(val value: StitcherBlockType) : IElementType(value.name, StitcherLang)

val PsiElement?.blockType: StitcherBlockType?
    inline get() = elementType?.blockType

val IElementType?.blockType: StitcherBlockType?
    get() = this?.takeAsOrNull<BlockIElementType>()?.value
