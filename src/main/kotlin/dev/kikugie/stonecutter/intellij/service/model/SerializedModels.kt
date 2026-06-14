@file:Suppress("unused")

package dev.kikugie.stonecutter.intellij.service.model

import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.Logger
import dev.kikugie.commons.collections.present
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.StringVersion
import dev.kikugie.semver.data.Version
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.jetbrains.plugins.gradle.model.ExternalProject
import java.io.File
import java.io.FileNotFoundException
import java.util.*

private val JSON = Json { ignoreUnknownKeys = true }

@Serializable
private class TreeModel(
    val stonecutter: String,
    val vcs: String,
    val current: String? = null,
    val branches: List<BranchInfo>,
    val nodes: List<NodeInfo>,
    val flags: Map<String, @Contextual Any>,
)

@Serializable
private class BranchModel(
    val id: String,
    val root: String,
    val nodes: List<NodeInfo>,
)

@Serializable
private class NodeModel(
    val project: String,
    val version: String = project,
    val active: Boolean = false,
    val branch: BranchInfo,
    val root: String,
    val parameters: ParametersModel,
)

@Serializable
private class BranchInfo(
    val id: String,
    val path: String
)

@Serializable
private class NodeInfo(
    val project: String,
    val version: String = project,
    val active: Boolean = false,
    val path: String
) {
    fun build(): SCProjectMetadata = SCProjectMetadata(
        project, version, active
    )
}

@Serializable
private class ParametersModel(
    val constants: Map<String, Boolean> = emptyMap(),
    val dependencies: Map<String, @Serializable(with = VersionSerializer::class) Version> = emptyMap(),
    val swaps: Map<String, String> = emptyMap(),
    val replacements: List<ReplacementModel> = emptyList()
) {
    fun build(): SCProjectParameters = SCProjectParameters(
        constants, dependencies, swaps, replacements.map(ReplacementModel::build)
    )
}

@Serializable(with = ReplacementModel.Serializer::class)
private sealed interface ReplacementModel {
    fun build(): Replacement

    @Serializable
    class Str(
        val sources: Set<String> = emptySet(),
        val pattern: String? = null,
        val target: String,
        val identifier: String? = null,
    ) : ReplacementModel {
        override fun build(): Replacement = StringReplacement(setOfNotNull(pattern) + sources, target, identifier)
    }

    @Serializable
    class RegExp(
        val pattern: String? = null,
        val flags: Int = 0,
        val regex: RegexModel? = null,
        val target: String,
        val identifier: String? = null,
    ) : ReplacementModel {
        override fun build(): Replacement = RegexReplacement(regex?.pattern ?: pattern!!, buildOptions(regex?.flags ?: flags), target, identifier)

        private fun buildOptions(value: Int) = EnumSet.allOf(RegexOption::class.java)
            .apply { retainAll { value and it.mask == it.value } }
            .let(Collections::unmodifiableSet)
    }

    @Serializable
    class Perl(
        val pattern: String,
        val flags: Long = 0L,
        val target: String,
        val identifier: String? = null
    ) : ReplacementModel {
        override fun build(): Replacement = PerlReplacement(pattern, flags, target, identifier)
    }

    @Serializable
    class RegexModel(val pattern: String, val flags: Int = 0)

    object Serializer : JsonContentPolymorphicSerializer<ReplacementModel>(ReplacementModel::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ReplacementModel> = when (element) {
            is JsonObject -> matchObjectType(element)
            else -> throw SerializationException("Unable to deserialize ${element::class.qualifiedName}")
        }

        private fun matchObjectType(element: JsonObject): DeserializationStrategy<ReplacementModel> {
            val type = element["type"]?.jsonPrimitive?.contentOrNull
                ?: throw SerializationException("Unable to determine replacement type")
            if ("StringReplacement" in type) return Str.serializer()
            if ("RegexReplacement" in type) return RegExp.serializer()
            if ("PerlReplacement" in type) return Perl.serializer()
            throw SerializationException("Unable to determine replacement type")
        }
    }
}

private object VersionSerializer : JsonTransformingSerializer<Version>(Version.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement = when (element) {
        is JsonPrimitive -> remapStringVersion(element)
        is JsonObject -> if ("components" in element) remapSemanticVersion(element) else element
        else -> element
    }

    private fun remapStringVersion(prim: JsonPrimitive): JsonObject = buildJsonObject {
        put("type", StringVersion::class.qualifiedName)
        put("value", prim)
    }

    private fun remapSemanticVersion(obj: JsonObject): JsonObject = buildJsonObject {
        for ((k, v) in obj) put(renameField(k), v)
    }

    private fun renameField(key: String) = when(key) {
        "preRelease" -> "pre_release"
        "buildMetadata" -> "build_metadata"
        else -> key
    }
}

internal suspend fun buildTreeModel(logger: Logger, project: ExternalProject): SCProjectTree? {
    val model: TreeModel = project.buildDir.readJsonFile(logger, "stonecutter-cache/tree.json")
        ?: return null
    val stonecutter = model.stonecutter.runCatching(SemanticVersion::parse).getOrNull()
        ?: SemanticVersion(0, preRelease = "unknown")
    val branches = model.branches.map { buildBranchModel(logger, project, it) ?: return null }
    return SCProjectTree(project, branches, model.flags, stonecutter, model.current, model.vcs)
}

private suspend fun buildBranchModel(logger: Logger, parent: ExternalProject, info: BranchInfo): SCProjectBranch? {
    val project: ExternalProject = if (info.id.isEmpty()) parent else parent.childProjects[info.id] ?: logger.run {
        warn("Branch '${info.id}' is not found in ${parent.childProjects.keys.present()}")
        return null
    }
    val model: BranchModel = project.buildDir.readJsonFile(logger, "stonecutter-cache/branch.json")
        ?: return null
    val nodes = model.nodes.map { buildNodeModel(logger, project, it) ?: return null }
    return SCProjectBranch(project, nodes, info.id)
}

private suspend fun buildNodeModel(logger: Logger, parent: ExternalProject, info: NodeInfo): SCProjectNode? {
    val project: ExternalProject = parent.childProjects[info.project] ?: logger.run {
        warn("Node '${info.project}' is not found in ${parent.childProjects.keys.present()}")
        return null
    }
    val model: NodeModel = project.buildDir.readJsonFile(logger, "stonecutter-cache/node.json")
        ?: return null
    return SCProjectNode(project, info.build(), model.parameters.build())
}

private suspend inline fun <reified T> File.readJsonFile(logger: Logger, file: String): T? =
    resolve(file).readJson(logger, serializer<T>())

@OptIn(ExperimentalSerializationApi::class)
private suspend fun <T> File.readJson(logger: Logger, serializer: DeserializationStrategy<T>): T? = readAction {
    if (!exists()) return@readAction null.also {
        FileNotFoundException(absolutePath).let(logger::warn)
    }

    runCatching { inputStream().use { JSON.decodeFromStream(serializer, it) } }.getOrElse {
        logger.error("Failed to deserialize $absolutePath", it)
        null
    }
}
