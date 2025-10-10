package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.editor.inspection.error
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterService

class MissingValueVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitConstant(o: PsiExpression.Constant) {
        o.registerInconsistency("constant") { it in constants }
    }

    override fun visitSwap(o: PsiSwap) {
        o.identifier?.registerInconsistency("swap") { it in swaps }
    }

    override fun visitReplacement(o: PsiReplacement) {
        o.registerInconsistency("replacement") { name -> replacements.any { it.identifier == name } }
    }

    override fun visitAssignment(o: PsiExpression.Assignment) {
        o.target?.registerInconsistency("dependency") { it in dependencies }
    }

    private fun PsiElement.registerInconsistency(type: String, selector: SCProcessProperties.(String) -> Boolean) = missingValues(selector = selector)
        .joinToString().let { if (it.isNotEmpty()) holder.error(this, "stonecutter.inspection.missing_value.$type", it) }

    private inline fun PsiElement.missingValues(name: String = text, crossinline selector: SCProcessProperties.(String) -> Boolean): Sequence<String> {
        val lookup = stonecutterService.lookup
        val siblings = (lookup.node(this) ?: return emptySequence()).siblings(lookup)
        return siblings.filterNot { it.params.selector(name) }.map { it.metadata.project }
    }
}