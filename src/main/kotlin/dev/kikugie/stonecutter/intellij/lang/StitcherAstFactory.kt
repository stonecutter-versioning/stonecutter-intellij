package dev.kikugie.stonecutter.intellij.lang

import com.intellij.lang.ASTFactory
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.templateLanguages.OuterLanguageElement
import com.intellij.psi.templateLanguages.OuterLanguageElementImpl
import com.intellij.psi.tree.IElementType
import dev.kikugie.stonecutter.intellij.lang.token.StitcherTypes

class StitcherAstFactory : ASTFactory() {
    override fun createLeaf(type: IElementType, text: CharSequence): LeafElement? {
        return if (type == StitcherTypes.Component.DEFINITION)
            OuterLanguageElementImpl(type, text)
        else super.createLeaf(type, text)
    }
}