package dev.kikugie.stonecutter.intellij.editor.completion

import com.intellij.patterns.InitialPatternCondition
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PsiElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.nextLeafs
import com.intellij.psi.util.prevLeafs
import com.intellij.util.ProcessingContext
import dev.kikugie.stonecutter.intellij.util.filterNotWhitespace
import org.toml.lang.psi.ext.elementType
import kotlin.reflect.KClass

object StitcherPatterns {
    inline fun <reified T : PsiElement> psiElement(): PsiElementPattern.Capture<T> = PlatformPatterns.psiElement(T::class.java)

    fun psiElementType(vararg types: IElementType) = Capture(psiTypeCondition(types))
    fun psiElementClass(vararg classes: KClass<out PsiElement>) = Capture(psiClassCondition(classes))

    fun psiTypeCondition(elements: Array<out IElementType>) = object : InitialPatternCondition<PsiElement>(PsiElement::class.java) {
        override fun accepts(o: Any?, context: ProcessingContext?): Boolean =
            if (o is PsiElement) o.elementType in elements else false
    }

    private fun psiClassCondition(classes: Array<out KClass<out PsiElement>>) = object : InitialPatternCondition<PsiElement>(PsiElement::class.java) {
        override fun accepts(o: Any?, context: ProcessingContext?): Boolean =
            if (o is PsiElement) classes.any { it.isInstance(o) } else false
    }

    fun beforeLeafCondition(vararg types: IElementType) = object : PatternCondition<PsiElement>("beforeLeafCondition") {
        override fun accepts(element: PsiElement, context: ProcessingContext): Boolean =
            element.nextLeafs.filterNotWhitespace().find { it.elementType in types } != null
    }

    fun afterLeafCondition(vararg types: IElementType) = object : PatternCondition<PsiElement>("afterLeafCondition") {
        override fun accepts(element: PsiElement, context: ProcessingContext): Boolean =
            element.prevLeafs.filterNotWhitespace().find { it.elementType in types } != null
    }

    class Capture<T : PsiElement>(condition: InitialPatternCondition<T>) : PsiElementPattern<T, Capture<T>>(condition)
}