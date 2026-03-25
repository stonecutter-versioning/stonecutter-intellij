@file:Suppress("UnstableApiUsage")

package dev.kikugie.stonecutter.intellij.service.gradle

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.PropertiesComponent
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext
import org.jetbrains.plugins.gradle.service.syncAction.GradleSyncListener
import kotlin.io.path.Path

object GradleReloadListener : GradleSyncListener {
    override fun onSyncPhaseCompleted(context: ProjectResolverContext, phase: org.jetbrains.plugins.gradle.service.syncAction.GradleSyncPhase) =
        kotlinx.coroutines.runBlocking { updateStonecutterService(context) }

    private suspend fun updateStonecutterService(context: ProjectResolverContext) {
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