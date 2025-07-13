package dev.kikugie.stonecutter.intellij.editor

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import dev.kikugie.stonecutter.intellij.lang.psi.*
import dev.kikugie.stonecutter.intellij.service.stonecutterService

class StitcherDocumentationTargetProvider : DocumentationTargetProvider {

    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        val isStitcherFile = file.language.displayName == "Stitcher"
        if (!isStitcherFile) return emptyList()

        val element = file.findElementAt(offset) ?: return emptyList()

        val targetElement = findDependencyElement(element)

        return if (targetElement != null) {
            listOf(StitcherDocumentationTarget(targetElement))
        } else {
            emptyList()
        }
    }

    private fun findDependencyElement(element: PsiElement): PsiElement? {
        if (element is StitcherDependency) {
            return element
        }

        // Check parent chain for dependency elements
        var parent = element.parent
        while (parent != null) {
            when (parent) {
                is StitcherDependency -> return parent
                is StitcherAssignment -> {
                    // For assignments, check if we're hovering over the dependency part
                    val dependency = findDependencyInAssignment(parent)
                    if (dependency != null) return dependency
                }
            }
            parent = parent.parent
        }

        return null
    }

    private fun findDependencyInAssignment(assignment: StitcherAssignment): StitcherDependency? {
        return assignment.children.filterIsInstance<StitcherDependency>().firstOrNull()
    }
}

class StitcherDocumentationTarget(private val element: PsiElement) : DocumentationTarget {

    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPointer = element.createSmartPointer()
        return Pointer {
            val restoredElement = elementPointer.element
            if (restoredElement != null) StitcherDocumentationTarget(restoredElement) else null
        }
    }

    override fun computePresentation(): TargetPresentation {
        return TargetPresentation.builder("Dependency: ${element.text}").presentation()
    }

    override fun computeDocumentation(): DocumentationResult? {
        val html = generateDependencyDocs(element.text)
        return DocumentationResult.documentation(html)
    }

    override fun computeDocumentationHint(): String? {
        val stonecutterNode = element.project.stonecutterService.lookup.node(element)
        val dependencyName = element.text

        // First try to get the value from StoneCutter dependencies
        val configuredValue = stonecutterNode?.params?.dependencies?.get(dependencyName)
        if (configuredValue != null) {
            return "$dependencyName = $configuredValue"
        }

        // Show that it's a dependency even if no value is configured
        return "$dependencyName (dependency)"
    }

    private fun generateDependencyDocs(name: String): String {
        val stonecutterNode = element.project.stonecutterService.lookup.node(element)
        val projectVersion = stonecutterNode?.metadata?.version

        return """
            <html><body>
            <h5>StoneCutter Dependency: $name</h5> 
            ${if (projectVersion != null) "<p><b>Current Version:</b> <code>$projectVersion</code></p>" else "<p><i>Could not fetch Version</i></p>"}
            <p><a href="https://stonecutter.kikugie.dev/wiki/config/params.html#condition-dependencies">Condition Dependencies →</a></p>
            </body></html>
        """.trimIndent()
    }
}