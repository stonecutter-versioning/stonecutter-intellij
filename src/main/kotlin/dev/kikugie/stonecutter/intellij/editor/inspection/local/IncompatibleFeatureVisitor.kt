package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.editor.inspection.error
import dev.kikugie.stonecutter.intellij.lang.impl.StitcherLexer
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiScope
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.util.antlrType
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import org.jetbrains.annotations.PropertyKey

private val SC8A1: SemanticVersion = SemanticVersion(0, 8, preRelease = "alpha.1")
private val SC9A1: SemanticVersion = SemanticVersion(0, 9, preRelease = "alpha.1")
private val SC9A3: SemanticVersion = SemanticVersion(0, 9, preRelease = "alpha.3")
private val SC10A1: SemanticVersion = SemanticVersion(0, 10, preRelease = "alpha.1")

class IncompatibleFeatureVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    private val PsiElement.stonecutter: SemanticVersion?
        get() = stonecutterNode?.tree?.stonecutter

    override fun visitComment(comment: PsiComment) {
        comment.check(SC8A1, "stonecutter.inspection.feature.0.8.comments")
    }

    override fun visitSwapOpener(swap: PsiSwap.Opener) {
        for (arg in swap.args) arg.check(SC8A1, "stonecutter.inspection.feature.0.8.swap_args")
    }

    override fun visitReplacementToggle(replacement: PsiReplacement.Toggle) {
        for ((i, entry) in replacement.entries.withIndex()) {
            entry.op?.check(SC9A1, "stonecutter.inspection.feature.0.9.repl_toggle")
            entry.lastChild?.takeIf { it.antlrType == StitcherLexer.COMMA }
                ?.check(SC10A1, "stonecutter.inspection.feature.0.10.comma")
            if (i > 0) entry.check(SC9A1, "stonecutter.inspection.feature.0.9.repl_multi")
        }
    }

    override fun visitReplacementLocal(replacement: PsiReplacement.Local) {
        replacement.check(SC9A3, "stonecutter.inspection.feature.0.9.repl_local")
        for ((i, entry) in replacement.entries.withIndex()) {
            if (i > 0) entry.check(SC10A1, "stonecutter.inspection.feature.0.10.repl_local_multi")
            entry.lastChild?.takeIf { it.antlrType == StitcherLexer.COMMA }
                ?.check(SC10A1, "stonecutter.inspection.feature.0.10.comma")
        }

        val opener = replacement.opener
        if (opener is PsiScope.Named) opener.check(SC10A1, "stonecutter.inspection.feature.0.10.named_scope")
    }

    override fun visitSwapLocal(swap: PsiSwap.Local) {
        swap.check(SC9A3, "stonecutter.inspection.feature.0.9.swap_local")
    }

    private fun PsiElement.check(version: SemanticVersion, @PropertyKey(resourceBundle = BUNDLE) key: String) {
        if ((stonecutter ?: return) < version) holder.error(this, key)
    }
}