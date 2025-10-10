package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap

class NamingConventionVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitConstant(o: PsiExpression.Constant) {
        if (!o.text.isConventional()) holder.registerProblem(
            o,
            StonecutterBundle.message("stonecutter.inspection.naming.constant"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitAssignment(o: PsiExpression.Assignment) {
        if (o.target?.text?.isConventional() == false) holder.registerProblem(
            o,
            StonecutterBundle.message("stonecutter.inspection.naming.dependency"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitSwap(o: PsiSwap) {
        if (o.identifier?.text?.isConventional() == false) holder.registerProblem(
            o,
            StonecutterBundle.message("stonecutter.inspection.naming.swap"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitReplacement(o: PsiReplacement) {
        if (!o.text.isConventional()) holder.registerProblem(
            o,
            StonecutterBundle.message("stonecutter.inspection.naming.replacement"),
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