@file:OptIn(ExperimentalContracts::class)

package dev.kikugie.stonecutter.intellij.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.Path

@Suppress("NOTHING_TO_INLINE")
inline infix fun <T> Any?.then(other: T): T = other
inline fun <reified T> Any.takeAs(): T? = this as? T
inline fun supported(condition: Boolean, message: () -> String) {
    contract { returns() implies condition }
    if (!condition) throw UnsupportedOperationException(message())
}

inline fun Nothing?.also(action: () -> Unit): Nothing? {
    action()
    return this
}

inline fun Boolean.whenIt(action: (Boolean) -> Unit): Boolean {
    contract { returns() implies this@whenIt }
    return also { if (this) action(true) }
}
inline fun Boolean.whenNot(action: (Boolean) -> Unit): Boolean {
    contract { returns() implies !this@whenNot }
    return also { if (!this) action(false) }
}
fun Iterable<*>.toStringList() = joinToString(prefix = "[", postfix = "]") { "'$it'" }
fun Map<*, *>.keysToString() = keys.toStringList()

fun VirtualFile.getStonecutterProjectPath(project: Project): Result<Path> {
    val index = ProjectFileIndex.getInstance(project)
    var root: VirtualFile? = index.getSourceRootForFile(this) ?: this
    while (root != null && root.isValid)
        if (root.children.none { it.nameSequence.startsWith("stonecutter.gradle") }) root = root.parent
        else return Result.success(Path(root.path))
    return invalidArg("No Stonecutter project found")
}