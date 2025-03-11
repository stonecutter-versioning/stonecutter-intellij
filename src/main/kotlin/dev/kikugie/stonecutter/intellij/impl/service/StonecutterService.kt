package dev.kikugie.stonecutter.intellij.impl.service

import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import dev.kikugie.stonecutter.intellij.impl.PluginAssets
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class StonecutterService(private val project: Project) {
    private var version: String? = null
    private val cache: MutableMap<Path, StonecutterModelLookup> = ConcurrentHashMap()

    fun lookup(file: PsiFile): StonecutterModelLookup = lookup(file.virtualFile)
    fun lookup(file: VirtualFile): StonecutterModelLookup = StonecutterModelLookup(file, project).getCached()

    private fun StonecutterModelLookup.getCached() : StonecutterModelLookup =
        root.getOrNull()?.let { cache.getOrPut(it) { this } } ?: this

    internal fun reset() {
        cache.clear()
        version = project.virtualFile?.let(::lookup)
            ?.treeModel?.getOrNull()?.stonecutter
        notifyVersionMismatch()
    }

    private fun notifyVersionMismatch() {
        if (version == null || version == COMPILED_VERSION) return
        val title = "Stonecutter version mismatch"
        val text = "Stonecutter Dev is built for $COMPILED_VERSION. Some features may not be fully supported, and unexpected issues can occur."
        val msg = NotificationGroupManager.getInstance()
            .getNotificationGroup("Stonecutter Notification Group")
            .createNotification(title, text, NotificationType.WARNING)
            .setIcon(PluginAssets.STONECUTTER)
        msg.configureDoNotAskOption("sc-mismatch", title)
        msg.addAction(NotificationAction.createSimple("Don't show again") {
            PropertiesComponent.getInstance(project).apply {
                setValue("Notification.DoNotAsk-sc-mismatch", true)
                setValue("Notification.DisplayName-DoNotAsk-sc-mismatch", title)
            }
            msg.expire()
        })
        msg.notify(project)
    }

    companion object {
        const val COMPILED_VERSION = "0.6-alpha.8"
        
        val Project.stonecutterService: StonecutterService
            get() = getService(StonecutterService::class.java)

        val Project.virtualFile: VirtualFile?
            get() = getBaseDirectories().firstOrNull()
    }
}

