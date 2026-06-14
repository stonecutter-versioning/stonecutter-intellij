@file:Suppress("UnstableApiUsage")

package dev.kikugie.stonecutter.intellij.service.gradle

import com.intellij.ide.impl.ProjectUtil
import dev.kikugie.stonecutter.intellij.service.model.GradleIdentityPath.Companion.toIdentityPath
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import org.jetbrains.plugins.gradle.model.DefaultExternalProject
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
        val candidates = context.buildProjectTree().filterWithStonecutter().toList()
        project.stonecutterService.reset(candidates, true)
    }
}

private fun ProjectResolverContext.buildProjectTree(): List<DefaultExternalProject> = buildList {
    val projects = allBuilds.asSequence()
        .flatMap { it.projects }
        .mapNotNull { getProjectModel(it, ExternalProject::class.java) }
        .filterIsInstance<DefaultExternalProject>()
        .sortedBy { it.toIdentityPath() }

    for (project in projects) if (project.identityPath == ":") this += project.clone() else {
        var parent = asSequence()
            .filter { project.projectDir.startsWith(it.projectDir) }
            .maxByOrNull { it.projectDir.absolutePath.length }
            ?: continue
        for (segment in project.toIdentityPath()) {
            val next = parent.childProjects[segment]
            if (next != null) parent = next else {
                parent.childProjects[segment] = project.clone()
                break
            }
        }
    }
}

private fun Iterable<ExternalProject>.filterWithStonecutter(): Sequence<ExternalProject> = sequence {
    for (project in this@filterWithStonecutter)
        if (project.buildFile?.name.orEmpty().startsWith("stonecutter.gradle")) yield(project)
        else yieldAll(project.childProjects.values.filterWithStonecutter())
}

private fun DefaultExternalProject.clone(): DefaultExternalProject = DefaultExternalProject().also {
    it.id = id
    it.path = path
    it.identityPath = identityPath
    it.name = name
    it.qName = qName
    it.description = description
    it.group = group
    it.version = version
    it.projectDir = projectDir
    it.buildDir = buildDir
    it.buildFile = buildFile
    it.externalSystemId = externalSystemId
}
