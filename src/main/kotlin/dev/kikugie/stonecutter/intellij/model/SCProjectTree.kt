@file:UseSerializers(PathSerializer::class)
package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.stonecutter.intellij.model.serialized.ActiveInfo
import dev.kikugie.stonecutter.intellij.model.serialized.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

@Serializable
data class SCProjectTree(
    override val hierarchy: GradleProjectHierarchy,
    override val location: Path,
    val vcs: String,
    val current: String?,
    val currentProvider: ActiveInfo = ActiveInfo.UNKNOWN,
    val branches: Collection<GradleProjectHierarchy>
) : GradleMember {
    /**Finds a branch with the given [name].*/
    fun branch(lookup: SCModelLookup, name: String): SCProjectBranch? =
        lookup.branches[hierarchy + name]

    /**Gets all branches in this tree.*/
    fun branches(lookup: SCModelLookup): Sequence<SCProjectBranch> = branches.asSequence()
        .mapNotNull(lookup.branches::get)

    /**Gets all nodes with the given [name].*/
    fun nodes(lookup: SCModelLookup, name: String): Sequence<SCProjectNode> =
        branches(lookup).mapNotNull { it.node(lookup, name) }

    /**Gets all nodes in this tree depth-first.*/
    fun nodes(lookup: SCModelLookup): Sequence<SCProjectNode> =
        branches(lookup).flatMap { it.nodes(lookup) }
}