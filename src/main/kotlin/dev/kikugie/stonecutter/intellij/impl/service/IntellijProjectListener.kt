package dev.kikugie.stonecutter.intellij.impl.service

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import dev.kikugie.stonecutter.intellij.impl.service.StonecutterService.Companion.stonecutterService

class IntellijProjectListener : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.stonecutterService.reset()
    }
}