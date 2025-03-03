package dev.kikugie.stonecutter.intellij.impl.service

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.project.ProjectManager
import dev.kikugie.stonecutter.intellij.impl.service.StonecutterService.Companion.stonecutterService
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension
import org.jetbrains.plugins.gradle.util.GradleConstants

class GradleReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) {
        val path = node.data.linkedExternalProjectPath
        val match = ProjectManager.getInstance().openProjects.find {
            ProjectDataManager.getInstance().getExternalProjectData(it , GradleConstants.SYSTEM_ID, path) != null
        }
        match?.stonecutterService?.run {
            reset()
        }
    }
}