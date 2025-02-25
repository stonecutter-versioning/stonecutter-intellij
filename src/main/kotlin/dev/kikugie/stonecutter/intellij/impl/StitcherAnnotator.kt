package dev.kikugie.stonecutter.intellij.impl

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.lang.token.StitcherTypes
import dev.kikugie.stonecutter.intellij.util.string
import org.toml.lang.psi.ext.elementType

class StitcherAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        validateParameters(element, holder)
    }

    private fun validateParameters(element: PsiElement, holder: AnnotationHolder) = when(element.elementType) {
        StitcherTypes.Primitive.CONSTANT -> element.ifNotRegistered(element.getBuildParameters()?.constants, holder) {
            "Constant '${element.string}' is not registered."
        }
        StitcherTypes.Primitive.DEPENDENCY -> element.ifNotRegistered(element.getBuildParameters()?.dependencies, holder) {
            "Dependency '${element.string}' is not registered."
        }
        StitcherTypes.Primitive.SWAP -> element.ifNotRegistered(element.getBuildParameters()?.swaps, holder) {
            "Swap '${element.string}' is not registered."
        }
        else -> Unit
    }

    private fun PsiElement.getBuildParameters() = modelLookup.active.getOrNull()?.parameters
    private fun PsiElement.isRegistered(container: Map<String, *>?) = container?.containsKey(string) == true
    private inline fun PsiElement.ifNotRegistered(container: Map<String, *>?, holder: AnnotationHolder, action: () -> String) {
        if (!isRegistered(container)) holder.newAnnotation(HighlightSeverity.ERROR, action())
            .range(this)
            .create()
    }
}