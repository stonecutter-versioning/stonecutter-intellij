package dev.kikugie.stonecutter.intellij.editor.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElementVisitor
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherVisitor

abstract class StitcherInspectionTool(private val instantiator: (ProblemsHolder) -> StitcherProblemsVisitor) : LocalInspectionTool(), DumbAware {
    abstract class StitcherProblemsVisitor(protected val holder: ProblemsHolder) : StitcherVisitor()
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = instantiator(holder)
}