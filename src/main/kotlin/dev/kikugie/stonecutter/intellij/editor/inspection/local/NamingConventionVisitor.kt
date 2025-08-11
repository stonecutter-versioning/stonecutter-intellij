package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.lang.access.ReferenceType.Companion.referenceType

class NamingConventionVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitElement(element: PsiElement) {
        val type = element.referenceType ?: return
        if (!element.text.isConventional()) holder.registerProblem(
            element,
            StonecutterBundle.message("stonecutter.inspection.naming.${type.id}"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    private fun Char.isReferenceStart() =
        this == '_' || this in 'a'..'z'

    private fun Char.isReferencePart() =
        isReferenceStart() || this in '0'..'9'

    private fun String.isConventional(): Boolean {
        if (isEmpty()) return false
        if (!first().isReferenceStart()) return false
        for (i in 1..<length)
            if (!get(i).isReferencePart()) return false
        return true
    }
}