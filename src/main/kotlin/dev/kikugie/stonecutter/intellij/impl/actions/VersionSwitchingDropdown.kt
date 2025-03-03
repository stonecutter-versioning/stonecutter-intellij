package dev.kikugie.stonecutter.intellij.impl.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.openapi.fileEditor.FileEditorManager
import dev.kikugie.stonecutter.intellij.impl.service.StonecutterService.Companion.stonecutterService
import javax.swing.JComponent

class VersionSwitchingDropdown : ComboBoxAction() {
    override fun getActionUpdateThread(): ActionUpdateThread =
        ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val name = getActiveVersionName(event)
        with(event.presentation) {
            isEnabledAndVisible = name != null
            text = name ?: "Unresolved"
        }
    }

    override fun createPopupActionGroup(button: JComponent, context: DataContext) = DefaultActionGroup().apply {
        val project = context.getData(CommonDataKeys.PROJECT) ?: return@apply
        val versions = ActionUtil.underModalProgress(project, "Loading available versions")
            { getVersionSelectors(context) }
        addAll(versions)
    }

    private fun getActiveVersionName(event: AnActionEvent): String? {
        val project = event.project ?: return null
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
            ?: return null

        return project.stonecutterService.lookup(file)
            .activeVersionInfo.getOrNull()?.metadata?.project
    }

    private fun getVersionSelectors(context: DataContext): List<VersionSwitchAction> {
        val editor = context.getData(CommonDataKeys.EDITOR) ?: return emptyList()
        val project = editor.project ?: return emptyList()
        val lookup = project.stonecutterService.lookup(editor.virtualFile)
        val directory = lookup.root.getOrNull() ?: return emptyList()

        return lookup.versionsInfo.getOrElse { emptyList() }.map {
            VersionSwitchAction.create(
                version = it.metadata.project,
                path = directory,
                isVcs = it == lookup.vcsVersionInfo.getOrNull()
            )
        }
    }
}