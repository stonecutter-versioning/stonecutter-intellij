package dev.kikugie.stonecutter.intellij.editor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherVisitor

/**
 * Implements a local inspection using a problem collecting [Visitor].
 *
 * The visitor should be implemented in the `visitor` subpackage, **without recursively traversing elements**.
 * The inspection should be implemented in `StitcherInspections.kt`, extending this class.
 *
 * @see InvariantValueInspection
 */
abstract class StitcherInspectionTool(private val instantiator: (ProblemsHolder) -> Visitor) : LocalInspectionTool(), DumbAware {
    abstract class Visitor(protected val holder: ProblemsHolder) : StitcherVisitor()
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = instantiator(holder)
}