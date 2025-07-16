package dev.kikugie.stonecutter.intellij.model

import com.intellij.psi.PsiElement

/**
 * Provides a lightweight way of storing Stonecutter hierarchies.
 *
 * The instance can be obtained with [PsiElement/Project.stonecutterService.lookup][dev.kikugie.stonecutter.intellij.service.stonecutterService].
 *
 * Properties [trees], [branches] and [nodes] are serialized to the project state,
 * so they only store [GradleProjectHierarchy] keys of the respective references
 * instead of the actual objects to deduplicate values.
 */
interface SCModelLookup {
    val trees: Map<GradleProjectHierarchy, SCProjectTree>
    val branches: Map<GradleProjectHierarchy, SCProjectBranch>
    val nodes: Map<GradleProjectHierarchy, SCProjectNode>

    val all: Sequence<GradleMember>
        get() = sequence {
            yieldAll(trees.values)
            yieldAll(branches.values)
            yieldAll(nodes.values)
        }

    /**
     * Gets the project node related to the provided [element].
     * This will only work reliably for elements in the active project after IntelliJ has finished resolving the Gradle project.
     * In other cases `null` will be returned.
     *
     * @see dev.kikugie.stonecutter.intellij.service.stonecutterNode
     */
    fun node(element: PsiElement): SCProjectNode?
}