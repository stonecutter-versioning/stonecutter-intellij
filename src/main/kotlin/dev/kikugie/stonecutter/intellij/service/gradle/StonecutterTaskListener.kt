package dev.kikugie.stonecutter.intellij.service.gradle

import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectId
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings
import org.jetbrains.plugins.gradle.util.GradleConstants

class StonecutterTaskListener : ExternalSystemTaskNotificationListener {
    private val switches: MutableSet<ExternalSystemTaskId> = mutableSetOf()

    override fun onTaskOutput(id: ExternalSystemTaskId, text: String, stdOut: Boolean) {
        if (!StonecutterSettings.STATE.refreshAfterSwitch) return
        if (!text.startsWith("> Task :stonecutterSwitchTo")) return
        switches += id
    }

    override fun onSuccess(id: ExternalSystemTaskId) {
        if (id !in switches) return
        val project = id.findProject() ?: return
        val directory = project.basePath ?: return
        val tracker = ExternalSystemProjectTracker.getInstance(project)

        tracker.markDirty(ExternalSystemProjectId(GradleConstants.SYSTEM_ID, directory))
        tracker.scheduleProjectRefresh()
    }

    override fun onEnd(id: ExternalSystemTaskId) {
        switches -= id
    }
}