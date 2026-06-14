package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.editor.inspection.error
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.service.model.SCProjectParameters
import dev.kikugie.stonecutter.intellij.service.model.siblings
import dev.kikugie.stonecutter.intellij.service.stonecutterNode

class MissingValueVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitConstant(constant: PsiExpression.Constant) {
        constant.registerInconsistency("constant") { it in constants }
    }

    override fun visitSwapOpener(swap: PsiSwap.Opener) {
        swap.identifier?.registerInconsistency("swap") { it in swaps }
    }

    override fun visitReplacementToggle(replacement: PsiReplacement.Toggle) {
        for (entry in replacement.entries)
            entry.identifier?.registerInconsistency("replacement") { name -> replacements.any { it.identifier == name } }
    }

    override fun visitAssignment(assignment: PsiExpression.Assignment) {
        assignment.target?.registerInconsistency("dependency") { it in dependencies }
    }

    private fun PsiElement.registerInconsistency(type: String, selector: SCProjectParameters.(String) -> Boolean) = missingValues(selector = selector)
        .joinToString().let { if (it.isNotEmpty()) holder.error(this, "stonecutter.inspection.missing_value.$type", it) }

    private inline fun PsiElement.missingValues(name: String = text, crossinline selector: SCProjectParameters.(String) -> Boolean): Sequence<String> {
        return stonecutterNode?.siblings.orEmpty().asSequence().filterNot { it.parameters.selector(name) }.map { it.metadata.project }
    }
}