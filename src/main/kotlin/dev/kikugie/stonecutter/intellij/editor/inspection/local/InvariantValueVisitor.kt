package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.Version
import dev.kikugie.semver.data.VersionPredicate
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherDependency
import dev.kikugie.stonecutter.intellij.lang.util.convert
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
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
    override fun visitConstant(element: StitcherConstant) {
        if (element.variance { constants[it] } != 1) return
        val value = element.stonecutterNode!!.params.constants[element.text].toString()
        holder.registerProblem(
            element,
            StonecutterBundle.message("stonecutter.inspection.invariant_value.constant", value),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitDependency(element: StitcherDependency) {
        if (element.variance { dependencies[it] } != 1) return
        val value = element.stonecutterNode!!.params.dependencies[element.text].toString()
        holder.registerProblem(
            element,
            StonecutterBundle.message("stonecutter.inspection.invariant_value.dependency", value),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    override fun visitAssignment(element: StitcherAssignment) {
        val lookup = element.stonecutterService.lookup
        val node = lookup.node(element) ?: return

        val dependency = element.dependency?.text.orEmpty()
        val versions = node.siblings(lookup)
            .mapNotNull { it.params.dependencies[dependency] }

        val predicates = element.predicateList
            .map(VersionPredicate::convert)

        val variants = versions.map { it.check(predicates) }
            .toSet()

        if (variants.size != 1) return
        holder.registerProblem(
            element,
            StonecutterBundle.message("stonecutter.inspection.invariant_value.assignment", variants.first()),
            ProblemHighlightType.WEAK_WARNING
        )
    }

    private fun <T : PsiElement> T.variance(selector: SCProcessProperties.(String) -> Any?): Int {
        val lookup = stonecutterService.lookup
        val node = lookup.node(this) ?: return 0
        val text = text
        return node.siblings(lookup)
            .distinctBy { it.params.selector(text) }
            .count()
    }

    private fun Version.check(predicates: Iterable<VersionPredicate>) = predicates.all { it(this) }
}