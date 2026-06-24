package dev.kikugie.stonecutter.intellij.service

import com.google.gson.Gson
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.withBackgroundProgress
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterIcons
import dev.kikugie.stonecutter.intellij.service.model.StonecutterModels
import dev.kikugie.stonecutter.intellij.service.model.buildTreeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.plugins.gradle.model.DefaultExternalProject
import org.jetbrains.plugins.gradle.model.ExternalProject
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

private const val PROJECTS_COMPONENT: String = "dev.kikugie.stonecutter.projects"
private const val BACKGROUND_PROCESS: String = "Updating Stonecutter models"
private val GSON: Gson = Gson()

@Service(Service.Level.PROJECT)
class StonecutterService(val project: Project, val scope: CoroutineScope) : Disposable.Default {
    init {
        val stored = PropertiesComponent.getInstance(project).getList(PROJECTS_COMPONENT)
            ?: emptyList()
        if (stored.isNotEmpty()) scope.launch { reset(projects(stored), false) }
        StonecutterCallbacks.invokeProjectLoad(this)
    }

    private val output: Path by lazy { Path(PathManager.getLogPath()).resolve("stonecutter-log/latest.log") }
    private val logger: Logger by lazy { SCLogger("StonecutterService", output) }
    var lookup: StonecutterModels = StonecutterModels()
        private set

    internal suspend fun reset(projects: List<ExternalProject>, save: Boolean): Unit = withBackgroundProgress(project, BACKGROUND_PROCESS) {
        runCatching { output }.onFailure { return@withBackgroundProgress }
        output.parent.createDirectories()
        output.writeText("", Charsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)

        val trees = projects.mapNotNull { buildTreeModel(logger, it) }
        if (trees.size != projects.size) notifyModuleReadError()
        lookup = StonecutterModels(trees)

        if (save && trees.size == projects.size) {
            val list = projects.map { GSON.toJson(it, DefaultExternalProject::class.java) }
            PropertiesComponent.getInstance(project).setList(PROJECTS_COMPONENT, list)
        }
        StonecutterCallbacks.invokeProjectReload(this@StonecutterService)
    }

    private fun projects(stored: List<String>): List<DefaultExternalProject> = stored.mapNotNull {
        GSON.runCatching { fromJson(it, DefaultExternalProject::class.java) }.getOrNull()
    }

    private fun notifyModuleReadError() {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("stonecutter-notifications")
            .createNotification(
                StonecutterBundle.message("stonecutter.notifications.reload"),
                StonecutterBundle.message("stonecutter.notifications.reload.description"),
                NotificationType.WARNING
            )
            .setIcon(StonecutterIcons.STONECUTTER)
        with(notification) {
            configureDoNotAskOption("sc-model-err", title)
            @Suppress("DialogTitleCapitalization")
            addAction(NotificationAction.createSimple(StonecutterBundle.message("stonecutter.notifications.reload.sync")) {
                val tracker = ExternalSystemProjectTracker.getInstance(project)
                tracker.markDirtyAllProjects()
                tracker.scheduleProjectRefresh()
            })
            addAction(NotificationAction.createSimple(StonecutterBundle.message("stonecutter.notifications.reload.logs")) {
                val vf = LocalFileSystem.getInstance().findFileByNioFile(output)?.takeIf(VirtualFile::exists)
                    ?: return@createSimple
                FileEditorManager.getInstance(project).openFile(vf, true, true)
            })
            addAction(NotificationAction.createSimple(StonecutterBundle.message("stonecutter.notifications.reload.hide")) {
                PropertiesComponent.getInstance(project).apply {
                    setValue("Notification.DoNotAsk-sc-model-err", true)
                    setValue("Notification.DisplayName-DoNotAsk-sc-model-err", title)
                }
                expire()
            })
            notify(project)
        }
    }
}
