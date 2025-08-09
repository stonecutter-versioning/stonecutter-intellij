package dev.kikugie.stonecutter.intellij.service.gradle

import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.platform.workspace.storage.MutableEntityStorage
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import org.jetbrains.plugins.gradle.model.ExternalProject
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext
import org.jetbrains.plugins.gradle.service.syncAction.GradleSyncContributor
import kotlin.io.path.Path

@Suppress("UnstableApiUsage")
class GradleReloadListener : GradleSyncContributor {
    override suspend fun onModelFetchCompleted(context: ProjectResolverContext, storage: MutableEntityStorage) {
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