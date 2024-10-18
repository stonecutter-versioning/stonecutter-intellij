package dev.kikugie.stonecutter.intellij.impl

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level.PROJECT
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.gradle.service.project.AbstractProjectResolverExtension

val PsiElement.stonecutterService: StonecutterService
    get() = project.stonecutterService
val Project.stonecutterService: StonecutterService
    get() = getService(StonecutterService::class.java)

class ReloadListener : AbstractProjectResolverExtension() {
    @Suppress("UnstableApiUsage")
    override fun resolveFinished(node: DataNode<ProjectData>) = ProjectManager.getInstance().openProjects.forEach {
        it.stonecutterService.reset()
    }
}

@Service(PROJECT)
class StonecutterService(private val project: Project) {
    internal fun reset() {
    }
}