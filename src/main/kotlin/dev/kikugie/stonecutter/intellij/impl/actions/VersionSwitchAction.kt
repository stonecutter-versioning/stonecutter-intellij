package dev.kikugie.stonecutter.intellij.impl.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import dev.kikugie.stonecutter.intellij.impl.PluginAssets
import dev.kikugie.stonecutter.intellij.util.GradleUtil
import dev.kikugie.stonecutter.intellij.util.switchTask
import java.nio.file.Path
import javax.swing.Icon

class VersionSwitchAction(title: String, val task: String, val path: Path, icon: Icon) : AnAction(title, null, icon) {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
            ?: return Messages.showMessageDialog("Couldn't access the project", "Error", null)
        GradleUtil.runGradleTask(project, path) {
            taskNames = listOf(task)
        }
    }

    companion object {
        fun create(version: String, task: String = switchTask(version), path: Path, isVcs: Boolean = false) = VersionSwitchAction(
            version, task, path, if (isVcs) PluginAssets.VERSION_VCS else PluginAssets.VERSION_ENTRY
        )
    }
}