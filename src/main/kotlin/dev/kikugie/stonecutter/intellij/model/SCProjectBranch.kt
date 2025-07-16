@file:UseSerializers(PathSerializer::class)
package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.stonecutter.intellij.model.serialized.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

@Serializable
data class SCProjectBranch(
    override val hierarchy: GradleProjectHierarchy,
    override val location: Path,
    val id: String,
    val tree: GradleProjectHierarchy,
    val nodes: Collection<GradleProjectHierarchy>
) : GradleMember {
    /**Gets the tree of this branch, throwing [IllegalStateException] if it's missing for some reason.*/
    fun tree(lookup: SCModelLookup): SCProjectTree =
        checkNotNull(lookup.trees[tree]) { "Expected tree $tree for branch $hierarchy" }

    /**Finds a branch in the same tree with the given [name].*/
    fun peer(lookup: SCModelLookup, name: String): SCProjectBranch? =
        tree(lookup).branch(lookup, name)

    /**Gets all branches in the same tree.*/
    fun peers(lookup: SCModelLookup): Sequence<SCProjectBranch> =
        tree(lookup).branches(lookup)

    /**Finds a node in this branch with the given [name] matching [metadata.project][SCProjectNode.metadata].*/
    fun node(lookup: SCModelLookup, name: String): SCProjectNode? =
        lookup.nodes[hierarchy + name]

    /**Gets all nodes in this branch.*/
    fun nodes(lookup: SCModelLookup): Sequence<SCProjectNode> = nodes.asSequence()
        .mapNotNull(lookup.nodes::get)
}