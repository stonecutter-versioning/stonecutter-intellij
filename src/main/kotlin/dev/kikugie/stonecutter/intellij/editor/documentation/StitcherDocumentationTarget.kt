@file:Suppress("UnstableApiUsage")

package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.model.Pointer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.VfsPresentationUtil
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer

class StitcherDocumentationTarget<T : PsiElement>(private val element: T, private val builder: DocumentationBuilder<T>) : DocumentationTarget {
    companion object {
        const val ICONS_FQN = "dev.kikugie.stonecutter.intellij.StonecutterIcons"

        init {
            // For some reason preloading the class here is needed for it to be available in the HTML
            this::class.java.classLoader.loadClass(ICONS_FQN)
        }
    }

    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPointer = element.createSmartPointer()
        return Pointer { elementPointer.element?.let { StitcherDocumentationTarget(it, builder) } }
    }

    override fun computePresentation(): TargetPresentation = computePresentation(element.containingFile?.virtualFile)

    private fun computePresentation(file: VirtualFile?) = TargetPresentation.Companion
        .builder(element.text)
        .backgroundColor(file?.let { VfsPresentationUtil.getFileBackgroundColor(element.project, it) })
        .presentation()

    override fun computeDocumentation() = DocumentationResult.documentation(generateDependencyDocs())

    private fun generateDependencyDocs(): String = buildString {
        append("<html><body>")
        builder.applyTo(this, element)
        append("</body></html>")
    }
}
