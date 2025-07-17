package dev.kikugie.stonecutter.intellij.editor.inspection.outer

import com.intellij.codeInsight.intention.FileModifier
import com.intellij.codeInspection.*
import com.intellij.codeInspection.util.IntentionFamilyName
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.getAndUpdateUserData
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.LeafPsiElement
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.lang.util.isInjected
import dev.kikugie.stonecutter.intellij.util.childrenSequence
import kotlin.math.min

class LateReplacementInspection : LocalInspectionTool() {
    override fun runForWholeFile(): Boolean = true
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
        if (session.file.isInjected) PsiElementVisitor.EMPTY_VISITOR else Visitor(session)

    override fun inspectionFinished(session: LocalInspectionToolSession, holder: ProblemsHolder) {
        val positions = session.getUserData(Constants.POSITIONS_KEY).orEmpty()
        val halt = session.getUserData(Constants.HALT_KEY) ?: -1
        if (halt < 0 || positions.isEmpty()) return

        val elements = positions.asSequence().filter { it > halt }.mapNotNull(session.file::findElementAt)
        reportReplacements(holder, elements)
    }

    private fun reportReplacements(holder: ProblemsHolder, replacements: Sequence<PsiElement>) {
        for (it in replacements) holder.registerProblem(
            it, StonecutterBundle.message("stonecutter.inspection.late_replacement.message"),
            ProblemHighlightType.WARNING, MoveCommentFix
        )
    }

    private class Visitor(val session: LocalInspectionToolSession) : PsiElementVisitor() {
        override fun visitWhiteSpace(space: PsiWhiteSpace) {
            ProgressIndicatorProvider.checkCanceled() // no-op
        }

        override fun visitElement(element: PsiElement) {
            ProgressIndicatorProvider.checkCanceled()
            if (element is LeafPsiElement) session.getAndUpdateUserData(Constants.HALT_KEY) {
                if (it == null) element.textOffset else min(it, element.textOffset)
            }
        }

        override fun visitComment(element: PsiComment) {
            ProgressIndicatorProvider.checkCanceled()
            if (ElementManipulators.getValueText(element).startsWith('~')) session.getAndUpdateUserData(Constants.POSITIONS_KEY) {
                it?.apply { add(element.textOffset) } ?: mutableListOf(element.textOffset)
            }
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

    private object Constants {
        val POSITIONS_KEY: Key<MutableList<Int>> = Key("STITCHER_REPLACEMENT_POSITIONS")
        val HALT_KEY: Key<Int> = Key("STITCHER_HALT_POSITION")
    }
}