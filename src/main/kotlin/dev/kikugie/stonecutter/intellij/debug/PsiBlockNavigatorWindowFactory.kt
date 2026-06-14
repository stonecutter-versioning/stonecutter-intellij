package dev.kikugie.stonecutter.intellij.debug

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class PsiBlockNavigatorWindowFactory : ToolWindowFactory, DumbAware {
    override suspend fun isApplicableAsync(project: Project): Boolean =
        ApplicationManager.getApplication().isInternal

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = PsiBlockNavigatorPanel(project, toolWindow)
        val content = toolWindow.contentManager.factory.createContent(panel, null, false)
        toolWindow.contentManager.addContent(content)
    }
}
