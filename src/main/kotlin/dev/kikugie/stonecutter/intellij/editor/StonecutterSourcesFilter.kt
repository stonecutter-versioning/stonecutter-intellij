package dev.kikugie.stonecutter.intellij.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.GeneratedSourcesFilter
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import kotlin.io.path.invariantSeparatorsPathString

private const val GENERATED_SRC = "build/generated/stonecutter"
private const val CACHE_SRC = "build/stonecutter-cache"

class StonecutterSourcesFilter : GeneratedSourcesFilter() {
    override fun isGeneratedSource(file: VirtualFile, project: Project): Boolean {
        val path = file.toNioPathOrNull()?.invariantSeparatorsPathString
            ?: return false
        return GENERATED_SRC in path || CACHE_SRC in path
    }
}