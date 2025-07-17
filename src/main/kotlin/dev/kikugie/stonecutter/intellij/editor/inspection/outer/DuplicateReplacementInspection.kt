package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getAndUpdateUserData
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherReplacement
import dev.kikugie.stonecutter.intellij.lang.util.commentDefinition
import dev.kikugie.stonecutter.intellij.lang.util.isInjected
import java.util.concurrent.atomic.AtomicBoolean

class DuplicateReplacementInspection : LocalInspectionTool() {
    override fun runForWholeFile(): Boolean = true
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
        if (session.file.isInjected) PsiElementVisitor.EMPTY_VISITOR else Visitor(holder, session)

    private class Visitor(val holder: ProblemsHolder, val session: LocalInspectionToolSession) : PsiElementVisitor() {
        override fun visitComment(comment: PsiComment) {
            ProgressIndicatorProvider.checkCanceled()
            val definition = comment.commentDefinition?.element as? StitcherReplacement
                ?: return
            val new = AtomicBoolean()
            session.getAndUpdateUserData(Constants.SEEN_KEY) {
                (it ?: mutableSetOf()).apply { new.set(add(definition.text)) }
            }

            if (!new.get()) holder.registerProblem(
                comment, StonecutterBundle.message("stonecutter.inspection.duplicate_replacement.message"),
                ProblemHighlightType.WEAK_WARNING, DeleteCommentFix
            )
        }
    }

    private object DeleteCommentFix : LocalQuickFix, FileModifier {
        override fun getFamilyName(): @IntentionFamilyName String =
            StonecutterBundle.message("stonecutter.inspection.duplicate_replacement.fix")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            descriptor.psiElement.delete()
        }
    }

    private object Constants {
        val SEEN_KEY: Key<MutableSet<String>> = Key("STITCHER_SEEN_REPLACEMENTS")
    }
}