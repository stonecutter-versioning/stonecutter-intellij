package dev.kikugie.stonecutter.intellij.service

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.psi.PsiElement
import com.intellij.util.io.write
import dev.kikugie.stonecutter.intellij.PluginAssets
import dev.kikugie.stonecutter.intellij.action.VersionSelectorAction
import dev.kikugie.stonecutter.intellij.model.*
import dev.kikugie.stonecutter.intellij.model.serialized.BranchInfo
import dev.kikugie.stonecutter.intellij.model.serialized.BranchModel
import dev.kikugie.stonecutter.intellij.model.serialized.NodeInfo
import dev.kikugie.stonecutter.intellij.model.serialized.NodeModel
import dev.kikugie.stonecutter.intellij.model.serialized.TreeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.GradleProjectResolverUtil
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext
import org.jetbrains.plugins.gradle.service.syncAction.GradleSyncContributor
import java.io.FileNotFoundException
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.notExists
import kotlin.io.path.writeText

val PsiElement.stonecutterService: StonecutterService
    get() = project.stonecutterService

val Project.stonecutterService: StonecutterService
    get() = getService(StonecutterService::class.java)

@Service(Service.Level.PROJECT)
class StonecutterService(private val project: Project, private val scope: CoroutineScope) {
    init {
        val stored = PropertiesComponent.getInstance(project).getList("dev.kikugie.stonecutter.projects")
            ?: emptyList()
        if (stored.isNotEmpty()) scope.launch {
            reset(stored.associate { val (a, b) = it.split('#'); a to b })
        }
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val output = Path(PathManager.getLogPath()).resolve("stonecutter-log/latest.log")
    private val logger = SCLogger("StonecutterService", output)
    var lookup: StonecutterModelLookup = StonecutterModelLookupImpl()
        private set

    internal suspend fun reset(projects: Map<String, String>) = withBackgroundProgress(project, "Updating Stonecutter models") {
        output.parent.createDirectories()
        output.writeText("", Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
        lookup = StonecutterModelLookupImpl().apply {
            if (projects.isEmpty()) return@apply
            val result = projects.entries.fold(false) { acc, (location, path) ->
                buildTreeModel(Path(location), GradleProjectHierarchy(path)) || acc
            }

            if (!result) return@apply notifyModuleReadError()
        }

        val switch = ActionManager.getInstance().getAction("dev.kikugie.stonecutter.intellij.select_version") as VersionSelectorAction
        switch.isAvailable = true
    }

    private fun notifyModuleReadError() {
        val title = "Stonecutter model issues"
        val message = """
            Stonecutter Dev has encountered some issues while building the project model.
            This may be caused by using an unsupported Stonecutter version, and may result in undefined behaviour.
            For more information read the IDE logs.
        """.trimIndent().lineSequence().joinToString(" ")

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("stonecutter-notifications")
            .createNotification(title, message, NotificationType.WARNING)
            .setIcon(PluginAssets.STONECUTTER)
        with(notification) {
            configureDoNotAskOption("sc-model-err", title)
            addAction(NotificationAction.createSimple("Don't show again") {
                PropertiesComponent.getInstance(project).apply {
                    setValue("Notification.DoNotAsk-sc-model-err", true)
                    setValue("Notification.DisplayName-DoNotAsk-sc-model-err", title)
                }
                expire()
            })
            addAction(NotificationAction.createSimple("Open log file") {
                val vf = LocalFileSystem.getInstance().findFileByNioFile(output)?.takeIf(VirtualFile::exists)
                    ?: return@createSimple
                FileEditorManager.getInstance(project).openFile(vf, true, true)
            })
            notify(project)
        }
    }

    private suspend fun StonecutterModelLookupImpl.buildTreeModel(dir: Path, hierarchy: GradleProjectHierarchy): Boolean {
        val tree: TreeModel = dir.resolve("build/stonecutter-cache/tree.json").readJson(TreeModel.serializer())
            ?: return false

        if (tree.branches.isEmpty()) return true
        val result = tree.branches.fold(false) { acc, branch -> buildBranchModel(branch, hierarchy, tree.current) || acc }
        trees[hierarchy] = SCProjectTree(hierarchy, dir, tree.vcs, tree.current, tree.branches.map { hierarchy + it.id })
        return result
    }

    private suspend fun StonecutterModelLookupImpl.buildBranchModel(info: BranchInfo, parent: GradleProjectHierarchy, current: String?): Boolean {
        val data = info.path.resolve("build/stonecutter-cache/branch.json").readJson(BranchModel.serializer())
            ?: return false
        if (data.nodes.isEmpty()) return true
        val hierarchy = parent + info.id
        val result = data.nodes.fold(false) { acc, node -> buildNodeModel(node, hierarchy, current) || acc }
        branches[hierarchy] = SCProjectBranch(hierarchy, info.path, info.id, hierarchy, data.nodes.map { hierarchy + it.project })
        return result
    }

    private suspend fun StonecutterModelLookupImpl.buildNodeModel(info: NodeInfo, parent: GradleProjectHierarchy, current: String?): Boolean {
        val data = info.path.resolve("build/stonecutter-cache/node.json").readJson(NodeModel.serializer())
            ?: return false
        val hierarchy = parent + info.project
        val meta = SCProjectMetadata(info.project, info.version, info.project == current)
        val params = data.parameters.run {
            SCProcessProperties(constants, dependencies, swaps.keys, replacements.mapNotNull { it.identifier }.toSet())
        }
        nodes[hierarchy] = SCProjectNode(hierarchy, info.path, meta, parent, params)
        return true
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun <T> Path.readJson(serializer: DeserializationStrategy<T>): T? = readAction {
        if (notExists()) return@readAction null.also {
            FileNotFoundException(invariantSeparatorsPathString).let(logger::warn)
        }

        runCatching { inputStream().use { json.decodeFromStream(serializer, it) } }.getOrElse {
            logger.error("Failed to deserialize $invariantSeparatorsPathString", it)
            null
        }
    }
}

private class StonecutterModelLookupImpl(
    override val trees: MutableMap<GradleProjectHierarchy, SCProjectTree> = mutableMapOf(),
    override val branches: MutableMap<GradleProjectHierarchy, SCProjectBranch> = mutableMapOf(),
    override val nodes: MutableMap<GradleProjectHierarchy, SCProjectNode> = mutableMapOf(),
) : StonecutterModelLookup {
    override fun node(element: PsiElement): SCProjectNode? = element
        .let(ModuleUtil::findModuleForPsiElement)
        ?.let(GradleProjectResolverUtil::getGradleIdentityPathOrNull)
        ?.let { nodes[GradleProjectHierarchy(it)] }
}

@Suppress("UnstableApiUsage")
private class GradleReloadListener : GradleSyncContributor {
    override suspend fun onModelFetchCompleted(context: ProjectResolverContext, storage: MutableEntityStorage) {
        val project = ProjectUtil.findProject(context.projectPath.let(::Path)) ?: return
        val candidates = context.rootBuild.projects.mapNotNull { build ->
            context.getProjectModel(build, ExternalProject::class.java)
                ?.takeIf { it.buildFile?.name?.startsWith("stonecutter.gradle") == true }
        }.associate {
            it.projectDir.invariantSeparatorsPath to it.identityPath
        }

        project.stonecutterService.reset(candidates)
        PropertiesComponent.getInstance(project).setList("dev.kikugie.stonecutter.projects", candidates.map { (k, v) -> "$k#$v" })
    }
}