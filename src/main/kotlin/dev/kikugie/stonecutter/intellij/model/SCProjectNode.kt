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
    fun branch(lookup: SCModelLookup): SCProjectBranch =
        checkNotNull(lookup.branches[branch]) { "Expected branch $branch for node $hierarchy" }

    fun tree(lookup: SCModelLookup): SCProjectTree =
        branch(lookup).tree(lookup)

    fun peer(lookup: SCModelLookup, name: String): SCProjectNode? =
        lookup.branches[branch]?.node(lookup, name)

    fun peers(lookup: SCModelLookup): Sequence<SCProjectNode> =
        lookup.branches[branch]?.nodes(lookup).orEmpty()

    fun sibling(lookup: SCModelLookup, branch: GradleProjectHierarchy): SCProjectNode? =
        lookup.branches[branch]?.node(lookup, metadata.project)

    fun siblings(lookup: SCModelLookup): Sequence<SCProjectNode> = tree(lookup).branches(lookup)
        .mapNotNull { it.node(lookup, metadata.project) }

    fun all(lookup: SCModelLookup): Sequence<SCProjectNode> = tree(lookup).branches(lookup)
        .flatMap { it.nodes(lookup) }
}