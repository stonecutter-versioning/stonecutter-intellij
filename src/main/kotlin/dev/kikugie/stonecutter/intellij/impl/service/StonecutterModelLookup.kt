package dev.kikugie.stonecutter.intellij.impl.service

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.data.tree.BranchModel
import dev.kikugie.stonecutter.data.tree.NodeInfo
import dev.kikugie.stonecutter.data.tree.NodeModel
import dev.kikugie.stonecutter.data.tree.TreeModel
import dev.kikugie.stonecutter.intellij.impl.service.StonecutterService.Companion.stonecutterService
import dev.kikugie.stonecutter.intellij.util.getStonecutterProjectPath
import dev.kikugie.stonecutter.intellij.util.lazyResultMapping
import dev.kikugie.stonecutter.intellij.util.mapResult
import dev.kikugie.stonecutter.intellij.util.requireNotNullResult
import org.jetbrains.plugins.gradle.execution.build.CachedModuleDataFinder
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.invariantSeparatorsPathString

class StonecutterModelLookup internal constructor(file: VirtualFile, private val project: Project) {
    val root: Result<Path> = file.getStonecutterProjectPath(project)
    private val cache: MutableMap<NodeInfo, Result<NodeModel>> =
        ConcurrentHashMap()

    val versionsInfo get() = branchModel.map { it.nodes }
    val activeVersionModel get() = activeVersionInfo.mapResult(::node)
    val vcsVersionModel get() = vcsVersionInfo.mapResult(::node)

    val branchModel by root.lazyResultMapping { path ->
        BranchModel.Companion.load(path.resolve("build/stonecutter-cache"))
    }

    val treeModel by (::branchModel).lazyResultMapping { branch ->
        root.mapResult { TreeModel.Companion.load(it.resolve(branch.root).resolve("build/stonecutter-cache")) }
    }

    val activeVersionInfo by (::versionsInfo).lazyResultMapping {
        requireNotNullResult(it.find(NodeInfo::active)) { "No active version found" }
    }

    val vcsVersionInfo by (::versionsInfo).lazyResultMapping {nodes ->
        treeModel.mapResult { tree ->
            requireNotNullResult(nodes.find { it.metadata == tree.vcs }) { "No VCS version info found" }
        }
    }

    val moduleDataNode by root.lazyResultMapping {
        requireNotNullResult(CachedModuleDataFinder.Companion.findMainModuleData(project, it.invariantSeparatorsPathString)) {
            "Couldn't find matching Gradle project"
        }
    }

    val externalProject by root.lazyResultMapping {
        requireNotNullResult(ExternalProjectDataCache.getInstance(project).getRootExternalProject(it.invariantSeparatorsPathString)) {
            "Couldn't find matching Gradle project"
        }
    }

    fun node(info: NodeInfo): Result<NodeModel> = cache.getOrPut(info) {
        root.mapResult { NodeModel.Companion.load(it.resolve("${info.path}/build/stonecutter-cache")) }
    }

    companion object {
        val PsiElement.modelLookup: StonecutterModelLookup
            get() = project.stonecutterService.lookup(containingFile)
    }
}