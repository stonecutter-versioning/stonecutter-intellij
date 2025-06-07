package dev.kikugie.stonecutter.intellij.service

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.model.GradleProjectHierarchy
import dev.kikugie.stonecutter.intellij.model.SCProjectBranch
import dev.kikugie.stonecutter.intellij.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.model.SCProjectTree

interface StonecutterModelLookup {
    val trees: Map<GradleProjectHierarchy, SCProjectTree>
    val branches: Map<GradleProjectHierarchy, SCProjectBranch>
    val nodes: Map<GradleProjectHierarchy, SCProjectNode>

    fun node(element: PsiElement): SCProjectNode?
}