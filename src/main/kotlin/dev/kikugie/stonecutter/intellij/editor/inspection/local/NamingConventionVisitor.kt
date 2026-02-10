package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap

class NamingConventionVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitConstant(constant: PsiExpression.Constant) {
        if (!constant.text.isConventional()) holder.registerProblem(
            constant,
            StonecutterBundle.message("stonecutter.inspection.naming.constant"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitAssignment(assignment: PsiExpression.Assignment) {
        if (assignment.target?.text?.isConventional() == false) holder.registerProblem(
            assignment,
            StonecutterBundle.message("stonecutter.inspection.naming.dependency"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitSwapOpener(swap: PsiSwap.Opener) {
        if (swap.identifier?.text?.isConventional() == false) holder.registerProblem(
            swap,
            StonecutterBundle.message("stonecutter.inspection.naming.swap"),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitReplacementToggle(replacement: PsiReplacement.Toggle) {
        for (entry in replacement.entries) if (entry.identifier?.text?.isConventional() == false) holder.registerProblem(
            entry.identifier!!,
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