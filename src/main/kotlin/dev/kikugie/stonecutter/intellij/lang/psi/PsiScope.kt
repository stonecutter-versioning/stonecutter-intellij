package dev.kikugie.stonecutter.intellij.lang.psi

import com.intellij.lang.ASTNode
import dev.kikugie.stonecutter.intellij.lang.impl.PsiStitcherNodeImpl

sealed interface PsiScope : PsiStitcherNode {
    class Closed(node: ASTNode) : PsiStitcherNodeImpl(node), PsiScope

    class Lookup(node: ASTNode) : PsiStitcherNodeImpl(node), PsiScope
}