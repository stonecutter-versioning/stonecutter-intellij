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
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties
import dev.kikugie.stonecutter.intellij.service.stonecutterNode

class StitcherDocumentationTarget(private val element: PsiElement) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPointer = element.createSmartPointer()
        return Pointer { elementPointer.element?.let(::StitcherDocumentationTarget) }
    }

    override fun computePresentation(): TargetPresentation = computePresentation(element.containingFile?.virtualFile)

    private fun computePresentation(file: VirtualFile?) = TargetPresentation.Companion
        .builder(element.text)
        .backgroundColor(file?.let { VfsPresentationUtil.getFileBackgroundColor(element.project, it) })
        .presentation()

    override fun computeDocumentation() = DocumentationResult.Companion.documentation(generateDependencyDocs())

    private fun generateDependencyDocs(): String = buildString {
        val properties = element.stonecutterNode?.params
        append("<html><body><div class='definition'><pre>")
        resolveDependency(element, properties)
        append("</pre></div></body></html>")
    }

    private fun StringBuilder.resolveDependency(element: PsiElement, properties: SCProcessProperties?) = when (element) {
        is StitcherConstant -> DocumentationResolver.Constant.generate(this, element, properties)
        is StitcherAssignment -> DocumentationResolver.Dependency.generate(this, element, properties)
        else -> error("Unsupported element type for $element")
    }
}
