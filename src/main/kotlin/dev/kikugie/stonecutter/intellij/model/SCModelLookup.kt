package dev.kikugie.stonecutter.intellij.model

import com.intellij.psi.PsiElement

/**
 * Provides a lightweight way of storing Stonecutter hierarchies.
 *
 * Properties [trees], [branches] and [nodes] are serialized to the project state,
 * so they only store [GradleProjectHierarchy] keys of the respective references
 * instead of the actual objects to deduplicate values.
 */
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