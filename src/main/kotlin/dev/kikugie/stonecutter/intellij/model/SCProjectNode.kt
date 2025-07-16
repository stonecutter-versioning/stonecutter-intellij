@file:UseSerializers(PathSerializer::class)
package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.stonecutter.intellij.model.serialized.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

@Serializable
data class SCProjectNode(
    override val hierarchy: GradleProjectHierarchy,
    override val location: Path,
    val metadata: SCProjectMetadata,
    val branch: GradleProjectHierarchy,
    val params: SCProcessProperties
) : GradleMember {
    /**Gets the branch of this node, throwing [IllegalStateException] if it's missing for some reason.*/
    fun branch(lookup: SCModelLookup): SCProjectBranch =
        checkNotNull(lookup.branches[branch]) { "Expected branch $branch for node $hierarchy" }

    /**Gets the tree of this node, throwing [IllegalStateException] if it's missing for some reason.*/
    fun tree(lookup: SCModelLookup): SCProjectTree =
        branch(lookup).tree(lookup)

    /**Finds the node with the same [metadata] in the given [branch].*/
    fun peer(lookup: SCModelLookup, branch: GradleProjectHierarchy): SCProjectNode? =
        lookup.branches[branch]?.node(lookup, metadata.project)

    /**Gets all nodes with the same [metadata] in this tree.*/
    fun peers(lookup: SCModelLookup): Sequence<SCProjectNode> =
        tree(lookup).nodes(lookup, metadata.project)

    /**Gets a node with the same [name] in this branch.*/
    fun sibling(lookup: SCModelLookup, name: String): SCProjectNode? =
        branch(lookup).node(lookup, name)

    /**Gets all nodes in this branch.*/
    fun siblings(lookup: SCModelLookup): Sequence<SCProjectNode> =
        branch(lookup).nodes(lookup)

    /**Gets all nodes in this tree depth-first.*/
    fun all(lookup: SCModelLookup): Sequence<SCProjectNode> =
        tree(lookup).nodes(lookup)
}