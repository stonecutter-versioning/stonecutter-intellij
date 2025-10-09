package dev.kikugie.stonecutter.intellij.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType
import dev.kikugie.stonecutter.intellij.lang.util.antlrRule
import dev.kikugie.stonecutter.intellij.lang.util.antlrType

internal val PsiElement.childrenSequence: Sequence<PsiElement>
    get() = sequence {
        var child: PsiElement? = firstChild
        while (child != null) {
            yield(child)
            child = child.nextSibling
        }
    }

internal val PsiElement.reverseChildrenSequence: Sequence<PsiElement>
    get() = sequence {
        var child: PsiElement? = lastChild
        while (child != null) {
            yield(child)
            child = child.prevSibling
        }
    }

internal val PsiElement.prevSiblings: Sequence<PsiElement>
    get() = generateSequence({ prevSibling }, { it.prevSibling })

internal val PsiElement.nextSiblings: Sequence<PsiElement>
    get() = generateSequence({ nextSibling }, { it.nextSibling })

internal fun Sequence<PsiElement>.filterNotWhitespace(): Sequence<PsiElement> =
    filter { it !is PsiWhiteSpace }

internal fun Sequence<PsiElement>.elementOfType(type: IElementType): PsiElement? =
    find { it.elementType == type }

internal fun Sequence<PsiElement>.elementOfAnyType(vararg types: IElementType): PsiElement? =
    find { it.elementType in types }

internal fun Sequence<PsiElement>.elementOfToken(type: Int): PsiElement? =
    find { it.antlrType == type }

internal fun Sequence<PsiElement>.elementOfAnyToken(vararg types: Int): PsiElement? =
    find { it.antlrType in types }
