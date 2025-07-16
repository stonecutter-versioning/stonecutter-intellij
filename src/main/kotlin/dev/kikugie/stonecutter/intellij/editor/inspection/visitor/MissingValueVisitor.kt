package dev.kikugie.stonecutter.intellij.editor.inspection.visitor

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapId
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterService

class MissingValueVisitor(holder: ProblemsHolder) : StitcherInspectionTool.Visitor(holder) {
    override fun visitConstant(o: StitcherConstant) =
        o.registerInconsistency("constant", o.missingValues { it !in constants }.joinToString())

    override fun visitDependency(o: StitcherDependency) =
        o.registerInconsistency("dependency", o.missingValues { it !in dependencies }.joinToString())

    override fun visitSwapId(o: StitcherSwapId) =
        o.registerInconsistency("swap", o.missingValues { it !in swaps }.joinToString())

    private fun PsiElement.registerInconsistency(type: String, undefined: String) {
        if (undefined.isNotEmpty()) holder.registerProblem(
            this,
            StonecutterBundle.message("stonecutter.inspection.missing_value.$type", undefined),
            ProblemHighlightType.ERROR
        )
    }

    private inline fun PsiElement.missingValues(name: String = text, crossinline selector: SCProcessProperties.(String) -> Boolean): Sequence<String> {
        val lookup = stonecutterService.lookup
        val siblings = (lookup.node(this) ?: return emptySequence()).siblings(lookup)
        return siblings.filter { it.params.selector(name) }.map { it.metadata.project }
    }
}