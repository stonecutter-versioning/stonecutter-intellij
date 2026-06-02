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
    override fun onSyncPhaseCompleted(context: ProjectResolverContext, phase: org.jetbrains.plugins.gradle.service.syncAction.GradleSyncPhase) {
        if (phase == org.jetbrains.plugins.gradle.service.syncAction.GradleSyncPhase.PROJECT_MODEL_PHASE)
            kotlinx.coroutines.runBlocking { updateStonecutterService(context) }
    }

    private suspend fun updateStonecutterService(context: ProjectResolverContext) {
        val project = ProjectUtil.findProject(context.projectPath.let(::Path)) ?: return
        val candidates = context.allBuilds.asSequence()
            .flatMap { it.projects }
            .mapNotNull { context.getProjectModel(it, ExternalProject::class.java) }
            .filter { it.buildFile?.name?.startsWith("stonecutter.gradle") == true }
            .associate { it.projectDir.invariantSeparatorsPath to it.identityPath }

        project.stonecutterService.reset(candidates)
        PropertiesComponent.getInstance(project).setList("dev.kikugie.stonecutter.projects", candidates.map { (k, v) -> "$k#$v" })
    }
}