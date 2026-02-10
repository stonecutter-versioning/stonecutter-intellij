package dev.kikugie.stonecutter.intellij.editor.inspection

import dev.kikugie.stonecutter.intellij.editor.inspection.local.*

// Local
class InvariantValueInspection : StitcherLocalInspectionTool(::InvariantValueVisitor)

class MissingValueInspection : StitcherLocalInspectionTool(::MissingValueVisitor)

class NamingConventionInspection : StitcherLocalInspectionTool(::NamingConventionVisitor)

class IncompatibleFeatureInspection : StitcherLocalInspectionTool(::IncompatibleFeatureVisitor)

class SwapArgumentInspection : StitcherLocalInspectionTool(::SwapArgumentVisitor)
