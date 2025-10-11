package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.*
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getAndUpdateUserData
import com.intellij.psi.PsiComment
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherOuterInspectionTool
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import java.util.concurrent.atomic.AtomicBoolean

class DuplicateReplacementVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) :
    StitcherOuterInspectionTool.Visitor(holder, session) {
    companion object {
        val SEEN_KEY: Key<MutableSet<String>> = Key("STITCHER_SEEN_REPLACEMENTS")
    }

    private object DeleteCommentFix : LocalQuickFix, FileModifier {
        override fun getFamilyName(): @IntentionFamilyName String =
            StonecutterBundle.message("stonecutter.inspection.duplicate_replacement.fix")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            descriptor.psiElement.delete()
        }
    }

    override fun visitComment(comment: PsiComment) {
        ProgressIndicatorProvider.checkCanceled()
        val definition = comment.commentDefinition?.element ?: return
        if (definition.component !is PsiReplacement) return

        val new = AtomicBoolean()
        session.getAndUpdateUserData(SEEN_KEY) {
            (it ?: mutableSetOf()).apply { new.set(add(definition.text)) }
        }

        if (!new.get()) holder.registerProblem(
            comment, StonecutterBundle.message("stonecutter.inspection.duplicate_replacement.message"),
            ProblemHighlightType.WEAK_WARNING, DeleteCommentFix
        )
    }
}