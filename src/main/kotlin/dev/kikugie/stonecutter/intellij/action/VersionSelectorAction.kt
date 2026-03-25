package dev.kikugie.stonecutter.intellij.action

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import dev.kikugie.stonecutter.intellij.StonecutterIcons
import dev.kikugie.stonecutter.intellij.model.SCModelLookup
import dev.kikugie.stonecutter.intellij.model.SCProjectTree
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import dev.kikugie.stonecutter.intellij.util.GradleUtil
import java.awt.Dimension
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JComponent

class VersionSelectorAction : ComboBoxAction(), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val lookup = event.project?.stonecutterService?.lookup
        if (lookup != null) event.presentation.configurePresentation(lookup)
        else event.presentation.isEnabledAndVisible = false
    }

    private fun Presentation.configurePresentation(lookup: SCModelLookup): Unit = when (lookup.trees.size) {
        0 -> isEnabledAndVisible = false
        1 -> {
            val version = lookup.trees.values.first().current
            isEnabled = version != null
            isVisible = true
            text = version ?: "<detached>"
        }

        else -> {
            isEnabled = false
            isVisible = true
            text = "<multi-project>"
        }
    }

    override fun createActionPopup(context: DataContext, component: JComponent, callback: Runnable?): JBPopup {
        val project = context.getData(CommonDataKeys.PROJECT) ?: throw UnsupportedOperationException()
        val lookup = project.stonecutterService.lookup

        val available = lookup.trees.values.filter { it.current != null }
        return when(available.size) {
            0 -> JBPopupFactory.getInstance().createMessage("No available versions")
            1 -> createVersionList(lookup, available.first(), context, callback)
            else -> JBPopupFactory.getInstance().createMessage("Not yet implemented")
        }
    }

    private fun createVersionList(lookup: SCModelLookup, tree: SCProjectTree, context: DataContext, callback: Runnable?): JBPopup {
        val nodes = tree.branches.asSequence()
            .mapNotNull { lookup.branches[it] }
            .flatMap { it.nodes.mapNotNull(lookup.nodes::get) }
            .distinctBy { it.metadata.project }

        val actions = nodes.map {
            val project = it.metadata.project
            val icon = if (project == tree.vcs) StonecutterIcons.VERSION_VCS else StonecutterIcons.VERSION_ENTRY
            VersionSwitchAction(project, icon, "stonecutterSwitchTo$project", tree.location)
        }

        val group = DefaultActionGroup().apply { addAll(actions.toList()) }
        return JBPopupFactory.getInstance()
            .createActionGroupPopup("Select Version", group, context, false, shouldShowDisabledActions(), false, callback, maxRows, preselectCondition)
            .also { it.setMinimumSize(Dimension(minWidth, minHeight)) }
    }

    inner class VersionSwitchAction(title: String, icon: Icon, val task: String, val path: Path) : AnAction(title, null, icon) {
        override fun actionPerformed(event: AnActionEvent) = when(val project = event.project) {
            null -> Messages.showMessageDialog("Couldn't access the project", "Error", null)
            else -> GradleUtil.runGradleTask(project, path) { taskNames = listOf(task) }
        }
    }
}