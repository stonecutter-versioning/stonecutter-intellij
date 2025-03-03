package dev.kikugie.stonecutter.intellij.impl.service

import com.intellij.ide.util.runOnceForProject
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class IntellijProjectListener : ProjectActivity {
    @Suppress("UnstableApiUsage")
    override suspend fun execute(project: Project) {
        runOnceForProject(project, "notify-stonecutter-compatibility") {
            // TODO
        }
    }
}