package dev.kikugie.stonecutter.intellij.model

import com.intellij.psi.PsiElement

interface SCModelLookup {
    val trees: Map<GradleProjectHierarchy, SCProjectTree>
    val branches: Map<GradleProjectHierarchy, SCProjectBranch>
    val nodes: Map<GradleProjectHierarchy, SCProjectNode>

    val all: Sequence<GradleMember> get() = sequence {
        yieldAll(trees.values)
        yieldAll(branches.values)
        yieldAll(nodes.values)
    }

    fun isEmpty() = nodes.isEmpty()
    fun node(element: PsiElement): SCProjectNode?
}