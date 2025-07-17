package dev.kikugie.stonecutter.intellij.editor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.util.isInjected

/**
 * Implements an outer inspection using a problem collecting [Visitor].
 *
 * Outer inspections run for the host files, unlike [StitcherLocalInspectionTool],
 * which is used for comment-independent analysis.
 *
 * Visitors should be implemented in the `outer` subpackage and usually
 * override [PsiElementVisitor.visitComment] and access Stitcher files
 * with [PsiComment.commentDefinition][dev.kikugie.stonecutter.intellij.lang.util.commentDefinition].
 * **It must not be recursive and should be stateless.**
 *
 * Elements may be visited concurrently and in an arbitrary order,
 * so if you need to access the entire structure,
 * the best practice is to store relevant information in [Visitor.session]
 * with [UserDataHolder][com.intellij.openapi.util.UserDataHolder] keys
 * (**don't store [PsiElement][com.intellij.psi.PsiElement] instances in it;
 * record [PsiElement.textOffset][com.intellij.psi.PsiElement.getTextOffset] instead!**).
 * Then override [LocalInspectionTool.inspectionFinished] and process the stored data there,
 * registering relevant problems.
 */
abstract class StitcherOuterInspectionTool(private val instantiator: (ProblemsHolder, LocalInspectionToolSession) -> Visitor) :
    LocalInspectionTool(), DumbAware {
    abstract class Visitor(protected val holder: ProblemsHolder, protected val session: LocalInspectionToolSession) : PsiElementVisitor()
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
        if (session.file.isInjected) PsiElementVisitor.EMPTY_VISITOR else instantiator(holder, session)
}