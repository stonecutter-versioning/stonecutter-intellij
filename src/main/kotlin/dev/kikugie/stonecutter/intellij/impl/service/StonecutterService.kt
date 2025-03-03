package dev.kikugie.stonecutter.intellij.impl.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class StonecutterService(private val project: Project) {
    private val cache: MutableMap<Path, StonecutterModelLookup> = ConcurrentHashMap()

    fun lookup(file: PsiFile): StonecutterModelLookup = lookup(file.virtualFile)
    fun lookup(file: VirtualFile): StonecutterModelLookup = StonecutterModelLookup(file, project).run {
        root.getOrNull()?.let { cache.getOrPut(it) { this } } ?: this
    }

    internal fun reset() = cache.clear()

    companion object {
        val Project.stonecutterService: StonecutterService
            get() = getService(StonecutterService::class.java)
    }
}

