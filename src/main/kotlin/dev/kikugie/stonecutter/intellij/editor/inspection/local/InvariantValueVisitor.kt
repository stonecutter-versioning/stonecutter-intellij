package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.Version
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiPredicate
import dev.kikugie.stonecutter.intellij.service.model.SCProjectParameters
import dev.kikugie.stonecutter.intellij.service.model.siblings
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterParameters
import dev.kikugie.stonecutter.intellij.service.stonecutterService

/* TODO: Add a quick fix to safely remove the block.
The visitor should store a record of all invariant values and determine if the whole condition is constant.
In that case there are a couple steps to be done:
- Check the affected code block. If condition is always `true`, try uncommenting it.
  If the value is `false`, remove the code block.
- Remove the condition, and potentially its closer.
  If instead of the closer, there's `else`, apply reverse steps to the next block.
  If next is an `else if` extension, modify it to be a valid opener or merge with the previous block.
**Don't do this until the framework for manipulating code fragments is designed.**
 */
class InvariantValueVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    override fun visitConstant(constant: PsiExpression.Constant) {
        if (constant.variance { constants[it] } != 1) return
        val value = constant.stonecutterParameters?.constants?.get(constant.text) ?: return
        holder.registerProblem(
            constant,
            StonecutterBundle.message("stonecutter.inspection.invariant_value.constant", value),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitAssignment(assignment: PsiExpression.Assignment) {
        assignment.target?.let(::visitDependency)

        val dependency = assignment.target?.text.orEmpty()
        val versions = assignment.stonecutterNode?.siblings
            ?.mapNotNull { it.parameters.dependencies[dependency] }
            ?: return

        val predicates = assignment.predicates.mapNotNull(PsiPredicate::parsed)
            .toList()

        val variants = versions.map { it.check(predicates) }
            .toSet()

        if (variants.size == 1) holder.registerProblem(
            assignment,
            StonecutterBundle.message("stonecutter.inspection.invariant_value.assignment", variants.first()),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    private fun visitDependency(o: PsiElement) {
        if (o.variance { dependencies[it] } != 1) return
        val value = o.stonecutterParameters?.dependencies?.get(o.text).toString()
        holder.registerProblem(
            o,
            StonecutterBundle.message("stonecutter.inspection.invariant_value.dependency", value),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    private fun <T : PsiElement> T.variance(selector: SCProjectParameters.(String) -> Any?): Int {
        val text = text
        return stonecutterNode?.siblings.orEmpty().distinctBy { it.parameters.selector(text) }.count()
    }

    private fun Version.check(predicates: Iterable<VersionPredicate>): Boolean = predicates.all { it(this) }
}