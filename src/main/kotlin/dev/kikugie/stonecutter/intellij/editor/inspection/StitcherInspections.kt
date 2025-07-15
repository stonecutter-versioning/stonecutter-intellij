package dev.kikugie.stonecutter.intellij.editor.inspection

import dev.kikugie.stonecutter.intellij.editor.inspection.visitor.*

class InvariantValueInspection : StitcherInspectionTool(::InvariantValueVisitor)