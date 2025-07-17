package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherSwapId
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterService

class MissingValueVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitConstant(o: StitcherConstant) =
        o.registerInconsistency("constant", o.missingValues { it in constants })

    override fun visitDependency(o: StitcherDependency) =
        o.registerInconsistency("dependency", o.missingValues { it in dependencies })

    override fun visitReplacement(o: StitcherReplacement) =
        o.registerInconsistency("replacement", o.missingValues { name -> replacements.any { it.identifier == name } })

    override fun visitSwapId(o: StitcherSwapId) =
        o.registerInconsistency("swap", o.missingValues { it in swaps })

    private fun PsiElement.registerInconsistency(type: String, undefined: Sequence<String>) = undefined.joinToString().let {
        if (it.isNotEmpty()) holder.registerProblem(
            this,
            StonecutterBundle.message("stonecutter.inspection.missing_value.$type", it),
            ProblemHighlightType.ERROR
        )
    }

    private inline fun PsiElement.missingValues(name: String = text, crossinline selector: SCProcessProperties.(String) -> Boolean): Sequence<String> {
        val lookup = stonecutterService.lookup
        val siblings = (lookup.node(this) ?: return emptySequence()).siblings(lookup)
        return siblings.filterNot { it.params.selector(name) }.map { it.metadata.project }
    }
}