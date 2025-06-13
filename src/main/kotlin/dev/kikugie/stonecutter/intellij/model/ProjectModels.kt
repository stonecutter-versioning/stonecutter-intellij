@file:UseSerializers(PathSerializer::class)
package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.stonecutter.intellij.model.serialized.PathSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

interface GradleMember {
    val hierarchy: GradleProjectHierarchy
    val location: Path
}

@Serializable
data class SCProjectNode(
    override val hierarchy: GradleProjectHierarchy,
    override val location: Path,
    val metadata: SCProjectMetadata,
    val branch: GradleProjectHierarchy,
    val params: SCProcessProperties
) : GradleMember {
}

@Serializable
data class SCProjectBranch(
    override val hierarchy: GradleProjectHierarchy,
    override val location: Path,
    val id: String,
    val tree: GradleProjectHierarchy,
    val nodes: Collection<GradleProjectHierarchy>
) : GradleMember {
}

@Serializable
data class SCProjectTree(
    override val hierarchy: GradleProjectHierarchy,
    override val location: Path,
    val vcs: String,
    val current: String?,
    val branches: Collection<GradleProjectHierarchy>
) : GradleMember {
}