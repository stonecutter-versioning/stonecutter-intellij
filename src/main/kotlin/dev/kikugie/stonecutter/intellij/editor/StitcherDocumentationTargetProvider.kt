package dev.kikugie.stonecutter.intellij.editor

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents
import dev.kikugie.stonecutter.intellij.editor.documentation.StitcherDocumentationTarget
import dev.kikugie.stonecutter.intellij.lang.StitcherLang
import dev.kikugie.stonecutter.intellij.lang.psi.*

class StitcherDocumentationTargetProvider : DocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> =
        if (file.language != StitcherLang) emptyList()
        else listOfNotNull(file.findElementAt(offset)?.let(::findDocumentationTarget))

    private fun findDocumentationTarget(element: PsiElement): StitcherDocumentationTarget? = element.parents(true)
        .find { it is StitcherConstant || it is StitcherAssignment }
        ?.let { StitcherDocumentationTarget(it) }
}
