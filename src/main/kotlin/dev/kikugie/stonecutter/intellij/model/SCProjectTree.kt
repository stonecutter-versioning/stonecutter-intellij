@file:UseSerializers(PathSerializer::class)
package dev.kikugie.stonecutter.intellij.model

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
    val branches: Collection<GradleProjectHierarchy>
) : GradleMember {
    fun branch(lookup: SCModelLookup, name: String): SCProjectBranch? =
        lookup.branches[hierarchy + name]

    fun branches(lookup: SCModelLookup): Sequence<SCProjectBranch> = branches.asSequence()
        .mapNotNull(lookup.branches::get)
}