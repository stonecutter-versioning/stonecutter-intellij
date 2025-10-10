package dev.kikugie.stonecutter.intellij.editor.inspection.local

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.stonecutter.intellij.StonecutterBundle.BUNDLE
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherLocalInspectionTool
import dev.kikugie.stonecutter.intellij.editor.inspection.error
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.model.SCModelLookup
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import org.jetbrains.annotations.PropertyKey

private val SC8A1: SemanticVersion = SemanticVersion(intArrayOf(0, 8), "alpha.1")

class IncompatibleFeatureVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) : StitcherLocalInspectionTool.Visitor(holder, session) {
    private val lookup: SCModelLookup by lazy { session.file.stonecutterService.lookup }
    private val PsiElement.stonecutter: SemanticVersion?
        get() = stonecutterNode?.tree(lookup)?.stonecutter

    override fun visitComment(comment: PsiComment) {
        comment.check(SC8A1, "stonecutter.inspection.feature.0.8.comments")
    }

    override fun visitSwapArgs(args: PsiSwap.Args) {
        args.check(SC8A1, "stonecutter.inspection.feature.0.8.swap_args")
    }

    private fun PsiElement.check(version: SemanticVersion, @PropertyKey(resourceBundle = BUNDLE) key: String) {
        if ((stonecutter ?: return) < version) holder.error(this, key)
    }
}