package dev.kikugie.stonecutter.intellij.editor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.visitor.StitcherPsiVisitor

/**
 * Implements a local inspection using a problem collecting [Visitor].
 *
 * Local inspections run independently for each Stonecutter comment,
 * which may happen concurrently. For inspections needing context from the host file,
 * see [StitcherOuterInspectionTool].
 *
 * The visitor should be implemented in the `local` subpackage.
 * The order of visited elements is arbitrary, so it's preferable to
 * override only the relevant visit methods and query neighbouring elements.
 * **It must not be recursive and should be stateless.**
 *
 * @see InvariantValueInspection
 */
abstract class StitcherLocalInspectionTool(private val instantiator: (ProblemsHolder, LocalInspectionToolSession) -> Visitor) :
    LocalInspectionTool(), DumbAware {
    abstract class Visitor(protected val holder: ProblemsHolder, protected val session: LocalInspectionToolSession) : StitcherPsiVisitor()

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor =
        instantiator(holder, session)
}