package dev.kikugie.stonecutter.intellij.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.elementType

internal val PsiElement.childrenSequence: Sequence<PsiElement>
    get() = sequence {
        var child: PsiElement? = firstChild
        while (child != null) {
            yield(child)
            child = child.nextSibling
        }
    }

internal val PsiElement.prevSiblings: Sequence<PsiElement>
    get() = generateSequence({ prevSibling }, { it.prevSibling })

internal val PsiElement.nextSiblings: Sequence<PsiElement>
    get() = generateSequence({ nextSibling }, { it.nextSibling })

internal fun Sequence<PsiElement>.filterNotWhitespace(): Sequence<PsiElement> =
    filter { it !is PsiWhiteSpace }

internal fun PsiElement.childrenSeqOfType(type: IElementType): Sequence<PsiElement> =
    childrenSequence.filter { it.elementType == type }

internal inline fun <reified T : PsiElement> PsiElement.childrenSeqOfType(): Sequence<T> =
    childrenSequence.filterIsInstance<T>()