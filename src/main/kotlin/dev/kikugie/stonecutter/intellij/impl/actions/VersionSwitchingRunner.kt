package dev.kikugie.stonecutter.intellij.impl.actions

import com.intellij.ide.actions.runAnything.RunAnythingContext
import com.intellij.ide.actions.runAnything.RunAnythingContext.ModuleContext
import com.intellij.ide.actions.runAnything.RunAnythingContext.ProjectContext
import com.intellij.ide.actions.runAnything.RunAnythingUtil
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandLineProvider
import com.intellij.ide.actions.runAnything.getPath
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil.findProjectNode
import com.intellij.openapi.project.Project
import dev.kikugie.stonecutter.intellij.impl.PluginAssets
import dev.kikugie.stonecutter.intellij.util.GradleUtil
import dev.kikugie.stonecutter.intellij.util.REFRESH_ACTIVE_TASK
import dev.kikugie.stonecutter.intellij.util.RESET_ACTIVE_TASK
import dev.kikugie.stonecutter.intellij.util.SWITCH_TASK_TEMPLATE
import dev.kikugie.stonecutter.intellij.util.switchTask
import org.jetbrains.plugins.gradle.service.project.GradleTasksIndices
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants.SYSTEM_ID
import javax.swing.Icon
import kotlin.io.path.Path

class VersionSwitchingRunner : RunAnythingCommandLineProvider() {
    override fun getIcon(value: String): Icon = PluginAssets.STONECUTTER
    override fun getHelpIcon(): Icon = PluginAssets.STONECUTTER
    override fun getHelpGroupTitle(): String = "Stonecutter"
    override fun getHelpCommand(): String = "stonecutter"
    override fun getHelpCommandAliases(): List<String> = listOf("sc")
    override fun getHelpCommandPlaceholder(): String = "sc (sw [<path>:]<vers> | rl [<path>] | vcs [<path>])"

    override fun getCompletionGroupTitle(): String = "Stonecutter Actions"

    override fun suggestCompletionVariants(data: DataContext, command: CommandLine): Sequence<String> {
        val project = RunAnythingUtil.fetchProject(data)
        val context = data.getData(EXECUTING_CONTEXT) ?: ProjectContext(project)
        fun getSwitchTasks() = transformTasks(context, project) {
            if (SWITCH_TASK_TEMPLATE !in it) null
            else it.getTaskPrefix() + it.substringAfterLast(':').trimEnd('"').substringAfterLast(' ')
        }
        fun getRefreshTasks() = transformTasks(context, project) {
            if (REFRESH_ACTIVE_TASK !in it) null
            else it.getTaskPrefix().run { if (length > 1) drop(1) else this }
        }
        fun getResetTasks() = transformTasks(context, project) {
            if (RESET_ACTIVE_TASK !in it) null
            else it.getTaskPrefix().run { if (length > 1) drop(1) else this }
        }

        return when {
            command.completedParameters.isEmpty() ->
                getRefreshTasks().withArgument("rl") + getResetTasks().withArgument("vcs") + getSwitchTasks().withArgument("sw")
            "sw" in command -> getSwitchTasks()
            "rl" in command -> getRefreshTasks()
            "vcs" in command -> getResetTasks()
            else -> emptySequence()
        }
    }

    override fun run(data: DataContext, command: CommandLine): Boolean {
        val project = RunAnythingUtil.fetchProject(data)
        val context = data.getData(EXECUTING_CONTEXT) ?: ProjectContext(project)

        return when(command.parameters.firstOrNull()) {
            "sw" -> command.runStonecutterTask(context, project) {
                if (it.isBlank()) return false
                "${it.getTaskPrefix()}\"${switchTask(it.substringAfterLast(':'))}"
            }
            "rl" -> command.runStonecutterTask(context, project) {
                "${it.getTaskPrefix()}$REFRESH_ACTIVE_TASK"
            }
            "vcs" -> command.runStonecutterTask(context, project) {
                "${it.getTaskPrefix()}$REFRESH_ACTIVE_TASK"
            }
            else -> false
        }
    }

    private fun String.getTaskPrefix(): String {
        val split = lastIndexOf(':') + 1
        return if (split == 0) "" else this.substring(0, split)
    }

    private inline fun CommandLine.runStonecutterTask(context: RunAnythingContext, project: Project, param: (String) -> String) =
        runStonecutterTask(context, project, param(parameters.getOrElse(1) { "" }))

    private fun runStonecutterTask(context: RunAnythingContext, project: Project, task: String): Boolean {
        val directory = context.getWorkingDirectory() ?: return false
        GradleUtil.runGradleTask(project, Path(directory)) {
            taskNames = listOf(task)
        }
        return true
    }



    private fun transformTasks(context: RunAnythingContext, project: Project, transform: (String) -> String?): Sequence<String> =
        getAvailableTasks(context, project).mapNotNull(transform).organizeTaskList()

    private fun Sequence<String>.withArgument(prefix: String) = map { "$prefix $it" }

    private fun Sequence<String>.organizeTaskList(): Sequence<String> {
        val list = toMutableList()
        if (list.none { it.getTaskPrefix().length > 1 })
            list.removeIf { it.firstOrNull() == ':' }
        list.sortWith(Comparator.naturalOrder())
        return list.asSequence()
    }

    private fun getAvailableTasks(context: RunAnythingContext, project: Project) = context.getWorkingDirectory()
        ?.let { GradleTasksIndices.getInstance(project).getTasksCompletionVariances(it).keys.asSequence() }
        ?: emptySequence()

    private fun RunAnythingContext.getWorkingDirectory(): String? = when (this) {
        is ProjectContext -> getLinkedProjectPath() ?: getPath()
        is ModuleContext -> getLinkedModulePath() ?: getPath()
        else -> getPath()
    }

    private fun ProjectContext.getLinkedProjectPath(): String? = GradleSettings.getInstance(project)
        .linkedProjectsSettings.firstOrNull()
        ?.let { findProjectNode(project, SYSTEM_ID, it.externalProjectPath) }
        ?.data?.linkedExternalProjectPath

    private fun ModuleContext.getLinkedModulePath(): String? =
        ExternalSystemApiUtil.getExternalProjectPath(module)
}