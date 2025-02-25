package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.kikugie.stonecutter.data.tree.BranchModel
import dev.kikugie.stonecutter.data.tree.NodeInfo
import dev.kikugie.stonecutter.data.tree.NodeModel
import dev.kikugie.stonecutter.data.tree.TreeModel
import dev.kikugie.stonecutter.intellij.util.mapResult
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path

val Project.stonecutterService: StonecutterService
    get() = getService(StonecutterService::class.java)
val PsiElement.modelLookup: StonecutterService.ModelLookup
    get() = containingFile.project.stonecutterService.lookup(containingFile)

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.stonecutterService.reset()
    }
}

@Service(PROJECT)
class StonecutterService(project: Project) {
    private val index: ProjectFileIndex = ProjectFileIndex.getInstance(project)
    private val cache: MutableMap<Path, ModelLookup> = ConcurrentHashMap()

    fun lookup(file: PsiFile): ModelLookup = ModelLookup(file).cached
    internal fun reset() = cache.clear()

    inner class ModelLookup(file: PsiFile) {
        private val dir = "build/stonecutter-cache"
        internal val root: Result<Path> = file.runCatching {
            val content = requireNotNull(index.getContentRootForFile(file.virtualFile))
                { "File is not a part of the project" }
            val source = requireNotNull(Path(content.path).parent.takeIf { it.endsWith("src") })
                { "File is not in the project sources" }
            source.parent.also {
                cache.putIfAbsent(it, this@ModelLookup)
            }
        }
        internal val cached get() = root.getOrNull()?.let { cache[it] } ?: this

        val valid: Boolean = root.isSuccess

        val tree: Result<TreeModel> by lazy {
            branch.mapResult {
                root.map { p -> p.resolve(it.root) }
            }.mapResult {
                TreeModel.load(it.resolve(dir))
            }
        }

        val branch: Result<BranchModel> by lazy {
            root.mapResult { BranchModel.load(it.resolve(dir)) }
        }

        val versions: Result<List<NodeInfo>> by lazy {
            branch.map { it.nodes }
        }

        val active: Result<NodeModel> by lazy {
            versions.mapCatching {
                requireNotNull(versions.getOrThrow().find { it.active })
                    { "No active version found in the branch file" }
            }.mapResult {
                node(it)
            }
        }

        fun node(info: NodeInfo): Result<NodeModel> = root.mapResult {
            NodeModel.load(it.resolve("${info.path}/$dir"))
        }
    }
}