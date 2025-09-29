package dev.kikugie.stonecutter.intellij.editor.inspection

import com.intellij.codeInspection.LocalInspectionToolSession
import com.intellij.codeInspection.ProblemsHolder
import dev.kikugie.stonecutter.intellij.editor.inspection.local.*
import dev.kikugie.stonecutter.intellij.editor.inspection.outer.DuplicateReplacementVisitor
import dev.kikugie.stonecutter.intellij.editor.inspection.outer.LateReplacementVisitor

// Local
class InvariantValueInspection : StitcherLocalInspectionTool(::InvariantValueVisitor)

class MissingValueInspection : StitcherLocalInspectionTool(::MissingValueVisitor)

class NamingConventionInspection : StitcherLocalInspectionTool(::NamingConventionVisitor)

class IncompatibleFeatureInspection : StitcherLocalInspectionTool(::IncompatibleFeatureVisitor)

class SwapArgumentInspection : StitcherLocalInspectionTool(::SwapArgumentVisitor)

// Outer
class DuplicateReplacementInspection : StitcherOuterInspectionTool(::DuplicateReplacementVisitor)

class LateReplacementInspection : StitcherOuterInspectionTool(::LateReplacementVisitor) {
    override fun inspectionFinished(session: LocalInspectionToolSession, holder: ProblemsHolder) =
        LateReplacementVisitor.inspectionFinished(session, holder)
}