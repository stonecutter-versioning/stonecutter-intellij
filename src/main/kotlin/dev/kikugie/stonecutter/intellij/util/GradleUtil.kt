package dev.kikugie.stonecutter.intellij.util

import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import org.gradle.tooling.GradleConnectionException
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ResultHandler
import org.gradle.tooling.model.GradleProject
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.nio.file.Path

private typealias ESTES = ExternalSystemTaskExecutionSettings

object GradleUtil {
    inline fun runGradleTask(project: Project, dir: Path, configuration: ESTES.() -> Unit) =
        runGradleTask(project, dir, ESTES().apply(configuration))

    fun runGradleTask(project: Project, dir: Path, configuration: ESTES) {
        val settings = configuration.apply {
            externalSystemIdString = GradleConstants.SYSTEM_ID.id
            externalProjectPath = dir.toString().replace('\\', '/')
        }

        ExternalSystemUtil.runTask(settings, DefaultRunExecutor.EXECUTOR_ID, project, GradleConstants.SYSTEM_ID,
            null, ProgressExecutionMode.IN_BACKGROUND_ASYNC, false)
    }

    fun getGradleModel(dir: File, consumer: (Result<GradleProject>) -> Unit) {
        val handler = object : ResultHandler<GradleProject> {
            override fun onComplete(project: GradleProject) = consumer(Result.success(project))
            override fun onFailure(exception: GradleConnectionException) = consumer(Result.failure(exception))
        }
        val connector = GradleConnector.newConnector().forProjectDirectory(dir)
        connector.connect().use { it.getModel(GradleProject::class.java, handler) }
    }
}