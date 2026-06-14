@file:Suppress("unused")

package dev.kikugie.stonecutter.intellij.service.model

import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.PsiElement
import dev.kikugie.semver.data.SemanticVersion
import dev.kikugie.semver.data.Version
import dev.kikugie.stonecutter.intellij.service.model.GradleIdentityPath.Companion.toIdentityPath
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.GradleProjectResolverUtil

@JvmInline
value class GradleIdentityPath(private val path: String) : Comparable<GradleIdentityPath>, Iterable<String> {
    val isRoot: Boolean get() = path == ":"

    override fun toString(): String = path

    override fun iterator(): Iterator<String> = PathSegmentIterator(path)

    override fun compareTo(other: GradleIdentityPath): Int {
        val it1 = this.iterator()
        val it2 = other.iterator()

        while (it1.hasNext() && it2.hasNext()) {
            val cmp = it1.next() compareTo it2.next()
            if (cmp != 0) return cmp
        }

        return when {
            it1.hasNext() -> 1
            it2.hasNext() -> -1
            else -> 0
        }
    }

    fun orEmpty(): String = if (isRoot) "" else path

    fun resolve(subpath: String): GradleIdentityPath {
        val trimmed = subpath.trimStart(':')
        return GradleIdentityPath(if (isRoot) ":$trimmed" else "$path:$subpath")
    }

    companion object {
        fun ExternalProject.toIdentityPath(): GradleIdentityPath =
            GradleIdentityPath(identityPath)

        fun PsiElement.findIdentityPath(): GradleIdentityPath? = ModuleUtil.findModuleForPsiElement(this)
            ?.let(GradleProjectResolverUtil::getGradleIdentityPathOrNull)
            ?.let(::GradleIdentityPath)
    }

    private class PathSegmentIterator(private val path: String) : Iterator<String> {
        var cursor = 1

        override fun hasNext(): Boolean = cursor < path.length
        override fun next(): String = when (val index = path.indexOf(':', cursor)) {
            -1 -> path.substring(cursor).also { cursor = path.length }
            else -> path.substring(cursor, index).also { cursor = index + 1 }
        }
    }
}

class StonecutterModels() {
    val trees: Map<GradleIdentityPath, SCProjectTree>
        field = mutableMapOf()
    val branches: Map<GradleIdentityPath, SCProjectBranch>
        field = mutableMapOf()
    val nodes: Map<GradleIdentityPath, SCProjectNode>
        field = mutableMapOf()

    constructor(trees: Iterable<SCProjectTree>) : this() {
        trees.forEach(::register)
    }

    private fun register(tree: SCProjectTree) {
        trees[tree.project.toIdentityPath()] = tree
        for (branch in tree.branches.values) {
            branches[branch.project.toIdentityPath()] = branch
            for (node in branch.nodes.values) {
                nodes[node.project.toIdentityPath()] = node
            }
        }
    }
}

class SCProjectTree(
    val project: ExternalProject,
    val branches: Map<String, SCProjectBranch>,
    val flags: Map<String, Any>,
    val stonecutter: SemanticVersion,
    val current: String?,
    val vcs: String
) {
    val nodes: Sequence<SCProjectNode> = branches.values.asSequence().flatMap { it.nodes.values }

    constructor(project: ExternalProject, branches: Iterable<SCProjectBranch>, flags: Map<String, Any>, stonecutter: SemanticVersion, current: String?, vcs: String)
        : this(project, branches.associateBy(SCProjectBranch::id), flags, stonecutter, current, vcs)

    init {
        for (it in branches.values) it.tree = this
    }
}

class SCProjectBranch(
    val project: ExternalProject,
    val nodes: Map<String, SCProjectNode>,
    val id: String,
) {
    constructor(project: ExternalProject, nodes: Iterable<SCProjectNode>, id: String)
        : this(project, nodes.associateBy { it.metadata.project }, id)

    lateinit var tree: SCProjectTree

    init {
        for (it in nodes.values) it.branch = this
    }
}

class SCProjectNode(
    val project: ExternalProject,
    val metadata: SCProjectMetadata,
    val parameters: SCProjectParameters,
) {
    lateinit var branch: SCProjectBranch

    val tree: SCProjectTree
        inline get() = branch.tree
}

class SCProjectMetadata(
    val project: String,
    val version: String,
    val active: Boolean
)

class SCProjectParameters(
    val constants: Map<String, Boolean>,
    val dependencies: Map<String, Version>,
    val swaps: Map<String, String>,
    val replacements: List<Replacement>,
)

sealed interface Replacement {
    val identifier: String?
}

class StringReplacement(
    val patterns: Set<String>,
    val target: String,
    override val identifier: String?
) : Replacement

class RegexReplacement(
    val pattern: String,
    val options: Set<RegexOption>,
    val target: String,
    override val identifier: String?
) : Replacement

class PerlReplacement(
    val pattern: String,
    val flags: Long,
    val target: String,
    override val identifier: String?
) : Replacement

val SCProjectNode.siblings: Collection<SCProjectNode>
    inline get() = branch.nodes.values

fun List<Replacement>.named(id: String): Sequence<Replacement> =
    asSequence().filter { it.identifier == id }
