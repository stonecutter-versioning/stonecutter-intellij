package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.util.LITERALS
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import dev.kikugie.stonecutter.intellij.lang.util.elementOfToken

sealed interface PsiScope : PsiStitcherNode {
    class Closed(node: ASTNode) : PsiStitcherNodeImpl(node), PsiScope

    class Lookup(node: ASTNode) : PsiStitcherNodeImpl(node), PsiScope {
        val plus: PsiElement? get() = childrenSequence.elementOfToken(StitcherLexer.PLUS)
        val lookup: PsiElement? get() = lastChild?.takeIf { it.antlrType in LITERALS }
    }

    class Named(node: ASTNode) : PsiStitcherNodeImpl(node), PsiScope {
        val name: PsiElement? get() = lastChild?.takeIf { it.antlrType in LITERALS }
    }
}