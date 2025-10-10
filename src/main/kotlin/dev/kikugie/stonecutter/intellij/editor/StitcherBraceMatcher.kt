package dev.kikugie.stonecutter.intellij.editor

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherParser

class StitcherBraceMatcher : PairedBraceMatcher {
    private val BRACES: Array<BracePair> = arrayOf(
        BracePair(StitcherLang.tokenTypeOf(StitcherParser.LEFT_BRACE), StitcherLang.tokenTypeOf(StitcherParser.RIGHT_BRACE), false)
    )

    override fun getPairs(): Array<out BracePair?> = BRACES
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true
    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset
}