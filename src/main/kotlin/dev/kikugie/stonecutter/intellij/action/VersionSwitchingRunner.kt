package dev.kikugie.stonecutter.intellij.action

import com.intellij.ide.actions.runAnything.RunAnythingContext
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandLineProvider
import com.intellij.ide.actions.runAnything.getPath
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil.findProjectNode
import com.intellij.openapi.project.Project
import dev.kikugie.stonecutter.intellij.util.GradleUtil
import dev.kikugie.stonecutter.intellij.StonecutterIcons
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.plugins.gradle.util.GradleConstants.SYSTEM_ID
import javax.swing.Icon
import kotlin.io.path.Path

class VersionSwitchingRunner : RunAnythingCommandLineProvider() {
    override fun getIcon(value: String): Icon = StonecutterIcons.STONECUTTER
    override fun getHelpIcon(): Icon = StonecutterIcons.STONECUTTER
    override fun getHelpGroupTitle(): String = "Stonecutter"
    override fun getHelpCommand(): String = "stonecutter"
    override fun getHelpCommandAliases(): List<String> = listOf("sc")
    override fun getHelpCommandPlaceholder(): String = "sc (sw [<path>:]<vers> | rl [<path>] | vcs [<path>])"

    override fun getCompletionGroupTitle(): String = "Stonecutter Actions"

    override fun suggestCompletionVariants(dataContext: DataContext, commandLine: CommandLine): Sequence<String> {
        return emptySequence()
//        val project = RunAnythingUtil.fetchProject(dataContext)
//        val lookup = project.stonecutterService.lookup
//        val context = dataContext.getData(EXECUTING_CONTEXT) ?: RunAnythingContext.ProjectContext(project)
//
//        fun getSwitchTasks() = transformTasks(context, project) {
//            if (SWITCH_TASK_TEMPLATE !in it) null
//            else it.getTaskPrefix() + it.substringAfterLast(':').trimEnd('"').substringAfterLast(' ')
//        }
//        fun getRefreshTasks() = transformTasks(context, project) {
//            if (REFRESH_ACTIVE_TASK !in it) null
//            else it.getTaskPrefix().run { if (length > 1) drop(1) else this }
//        }
//        fun getResetTasks() = transformTasks(context, project) {
//            if (RESET_ACTIVE_TASK !in it) null
//            else it.getTaskPrefix().run { if (length > 1) drop(1) else this }
//        }
//
//        return when {
//            commandLine.completedParameters.isEmpty() ->
//                getRefreshTasks().withArgument("rl") + getResetTasks().withArgument("vcs") + getSwitchTasks().withArgument("sw")
//            "sw" in commandLine -> getSwitchTasks()
//            "rl" in commandLine -> getRefreshTasks()
//            "vcs" in commandLine -> getResetTasks()
//            else -> emptySequence()
//        }
    }

    override fun run(dataContext: DataContext, commandLine: CommandLine): Boolean {
        return false
//        val project = RunAnythingUtil.fetchProject(dataContext)
//        val context = dataContext.getData(EXECUTING_CONTEXT) ?: RunAnythingContext.ProjectContext(project)

//        return when(commandLine.parameters.firstOrNull()) {
//            "sw" -> commandLine.runStonecutterTask(context, project) {
//                if (it.isBlank()) return false
//                "${it.getTaskPrefix()}\"${switchTask(it.substringAfterLast(':'))}"
//            }
//            "rl" -> commandLine.runStonecutterTask(context, project) {
//                "${it.getTaskPrefix()}$REFRESH_ACTIVE_TASK"
//            }
//            "vcs" -> commandLine.runStonecutterTask(context, project) {
//                "${it.getTaskPrefix()}$REFRESH_ACTIVE_TASK"
//            }
//            else -> false
//        }
    }

    private fun String.getTaskPrefix(): String {
        val split = lastIndexOf(':') + 1
        return if (split == 0) "" else this.substring(0, split)
    }

    private inline fun CommandLine.runStonecutterTask(context: RunAnythingContext, project: Project, param: (String) -> String) =
        runStonecutterTask(context, project, param(parameters.getOrElse(1) { "" }))

    private fun runStonecutterTask(context: RunAnythingContext, project: Project, task: String): Boolean {
        val directory = context.getWorkingDirectory() ?: return false
        GradleUtil.runGradleTask(project, Path(directory)) { taskNames = listOf(task) }
        return true
    }

    private fun Sequence<String>.withArgument(prefix: String) = map { "$prefix $it" }


    private fun RunAnythingContext.getWorkingDirectory(): String? = when (this) {
        is RunAnythingContext.ProjectContext -> getLinkedProjectPath() ?: getPath()
        is RunAnythingContext.ModuleContext -> getLinkedModulePath() ?: getPath()
        else -> getPath()
    }

    private fun RunAnythingContext.ProjectContext.getLinkedProjectPath(): String? = GradleSettings.getInstance(project)
        .linkedProjectsSettings.firstOrNull()
        ?.let { findProjectNode(project, SYSTEM_ID, it.externalProjectPath) }
        ?.data?.linkedExternalProjectPath

    private fun RunAnythingContext.ModuleContext.getLinkedModulePath(): String? =
        ExternalSystemApiUtil.getExternalProjectPath(module)
}