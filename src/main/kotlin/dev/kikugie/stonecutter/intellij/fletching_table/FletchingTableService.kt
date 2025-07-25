package dev.kikugie.stonecutter.intellij.fletching_table

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.util.GradleUtil.findGradlePath
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path

@Service(Service.Level.PROJECT)
class FletchingTableService(val project: Project) {
    companion object {
        val PsiElement.fletchingTable: FletchingTableAccessor?
            get() = project.getService(FletchingTableService::class.java).compute(this)
    }

    private val accessorCache: MutableMap<Path, FletchingTableAccessor> = ConcurrentHashMap()

    init {
        registerFileChanges()
    }

    private fun compute(element: PsiElement): FletchingTableAccessor? {
        val path = element.findGradlePath() ?: return null
        return accessorCache.computeIfAbsent(path, ::FletchingTableAccessor)
    }

    private fun registerFileChanges() = project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
        override fun after(events: List<VFileEvent>) {
            val paths = events.asSequence().filter { "build/generated/ksp" in it.path }
                .map { Path(it.path) }.toSet().ifEmpty { return }
            for ((key, accessor) in accessorCache)
                if (accessor.resources.any { it in paths }) accessorCache.remove(key)
        }
    })
}