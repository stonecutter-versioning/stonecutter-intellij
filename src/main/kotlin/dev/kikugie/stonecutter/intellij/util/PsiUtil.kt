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

internal val PsiElement.prevSiblings: Sequence<PsiElement>
    get() = generateSequence({ prevSibling }, { it.prevSibling })

internal val PsiElement.nextSiblings: Sequence<PsiElement>
    get() = generateSequence({ nextSibling }, { it.nextSibling })

internal fun Sequence<PsiElement>.filterNotWhitespace(): Sequence<PsiElement> =
    filter { it !is PsiWhiteSpace }

internal fun PsiElement.childOfType(type: IElementType): PsiElement =
    childOfTypeOrNull(type) ?: error("No child with type $type found")

internal fun PsiElement.childOfTypeOrNull(type: IElementType): PsiElement? =
    childrenSequence.find { it.elementType == type }

internal fun PsiElement.tokenOfType(type: Int): PsiElement =
    tokenOfTypeOrNull(type) ?: error("No child with type $type found")

internal fun PsiElement.tokenOfTypeOrNull(type: Int): PsiElement? =
    childrenSequence.find { it.antlrType == type }

internal fun PsiElement.ruleOfType(type: Int): PsiElement =
    tokenOfTypeOrNull(type) ?: error("No child with type $type found")

internal fun PsiElement.ruleOfTypeOrNull(type: Int): PsiElement? =
    childrenSequence.find { it.antlrRule == type }

internal fun PsiElement.childrenSeqOfType(type: IElementType): Sequence<PsiElement> =
    childrenSequence.filter { it.elementType == type }

internal fun PsiElement.tokenSeqOfType(type: Int): Sequence<PsiElement> =
    childrenSequence.filter { it.antlrType == type }

internal fun PsiElement.ruleSeqOfType(type: Int): Sequence<PsiElement> =
    childrenSequence.filter { it.antlrRule == type }

internal inline fun <reified T : PsiElement> PsiElement.childrenSeqOfType(): Sequence<T> =
    childrenSequence.filterIsInstance<T>()