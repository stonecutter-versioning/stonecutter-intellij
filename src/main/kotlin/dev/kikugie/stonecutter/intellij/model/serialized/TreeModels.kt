@file:UseSerializers(PathSerializer::class, FlagContainerJsonSerializer::class)

package dev.kikugie.stonecutter.intellij.model.serialized

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.nio.file.Path

@Serializable
data class NodeInfo(
    val project: String,
    val version: String = project,
    val active: Boolean = false,
    val path: Path
)

@Serializable
data class BranchInfo(
    val id: String,
    val path: Path
)

@Serializable
data class NodeModel(
    val project: String,
    val version: String = project,
    val active: Boolean = false,
    val branch: BranchInfo,
    val root: Path,
    val parameters: BuildParameters,
)

@Serializable
data class BranchModel(
    val id: String,
    val root: Path,
    val nodes: List<NodeInfo>,
)

@Serializable
data class TreeModel(
    val stonecutter: String,
    val vcs: String,
    val current: String? = null,
    val branches: List<BranchInfo>,
    val nodes: List<NodeInfo>,
    val flags: Map<String, @Contextual Any>,
)