package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.*
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getAndUpdateUserData
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.editor.inspection.StitcherOuterInspectionTool
import dev.kikugie.stonecutter.intellij.lang.util.childrenSequence
import kotlin.math.min

class LateReplacementVisitor(holder: ProblemsHolder, session: LocalInspectionToolSession) :
    StitcherOuterInspectionTool.Visitor(holder, session) {
    companion object {
        val POSITIONS_KEY: Key<MutableList<Int>> = Key("STITCHER_REPLACEMENT_POSITIONS")
        val HALT_KEY: Key<Int> = Key("STITCHER_REPLACEMENT_LIMIT")

        fun inspectionFinished(session: LocalInspectionToolSession, holder: ProblemsHolder) {
            val positions = session.getUserData(POSITIONS_KEY).orEmpty()
            val halt = session.getUserData(HALT_KEY) ?: -1
            if (halt < 0 || positions.isEmpty()) return

            val elements = positions.asSequence().filter { it > halt }
                .mapNotNull(session.file::findElementAt)
            reportReplacements(holder, elements)
        }

        private fun reportReplacements(holder: ProblemsHolder, replacements: Sequence<PsiElement>) {
            for (it in replacements) holder.registerProblem(
                it, StonecutterBundle.message("stonecutter.inspection.late_replacement.message"),
                ProblemHighlightType.WARNING, MoveCommentFix
            )
        }
    }

    private object MoveCommentFix : LocalQuickFix, FileModifier {
        override fun getFamilyName(): @IntentionFamilyName String =
            StonecutterBundle.message("stonecutter.inspection.late_replacement.fix")

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) = with(descriptor) {
            val first = startElement.containingFile.childrenSequence.first()
            startElement.containingFile.addBefore(psiElement.copy(), first)
            psiElement.delete()
        }
    }


    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        ProgressIndicatorProvider.checkCanceled() // no-op
    }

    override fun visitElement(element: PsiElement) {
        ProgressIndicatorProvider.checkCanceled()
        if (element is LeafPsiElement) session.getAndUpdateUserData(HALT_KEY) {
            if (it == null) element.textOffset else min(it, element.textOffset)
        }
    }

    override fun visitComment(element: PsiComment) {
        ProgressIndicatorProvider.checkCanceled()
        if (ElementManipulators.getValueText(element).startsWith('~')) session.getAndUpdateUserData(POSITIONS_KEY) {
            it?.apply { add(element.textOffset) } ?: mutableListOf(element.textOffset)
        }
    }
}