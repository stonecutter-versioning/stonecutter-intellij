package dev.kikugie.stonecutter.intellij.service

import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.model.GradleMember
import dev.kikugie.stonecutter.intellij.model.GradleProjectHierarchy
import dev.kikugie.stonecutter.intellij.model.SCProjectBranch
import dev.kikugie.stonecutter.intellij.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.model.SCProjectTree

interface StonecutterModelLookup {
    val trees: Map<GradleProjectHierarchy, SCProjectTree>
    val branches: Map<GradleProjectHierarchy, SCProjectBranch>
    val nodes: Map<GradleProjectHierarchy, SCProjectNode>

    val all: Sequence<GradleMember> get() = sequence {
        yieldAll(trees.values)
        yieldAll(branches.values)
        yieldAll(nodes.values)
    }

    fun node(element: PsiElement): SCProjectNode?
}