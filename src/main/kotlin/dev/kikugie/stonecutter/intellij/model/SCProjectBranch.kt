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
    fun tree(lookup: SCModelLookup): SCProjectTree =
        checkNotNull(lookup.trees[tree]) { "Expected tree $tree for branch $hierarchy" }

    fun peer(lookup: SCModelLookup, name: String): SCProjectBranch? =
        tree(lookup).branch(lookup, name)

    fun peers(lookup: SCModelLookup): Sequence<SCProjectBranch> =
        tree(lookup).branches(lookup)

    fun node(lookup: SCModelLookup, name: String): SCProjectNode? =
        lookup.nodes[hierarchy + name]

    fun nodes(lookup: SCModelLookup): Sequence<SCProjectNode> = nodes.asSequence()
        .mapNotNull(lookup.nodes::get)
}