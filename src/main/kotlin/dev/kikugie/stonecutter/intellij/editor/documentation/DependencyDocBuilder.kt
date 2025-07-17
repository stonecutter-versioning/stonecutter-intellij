package dev.kikugie.stonecutter.intellij.editor.documentation

import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.html
import dev.kikugie.stonecutter.intellij.editor.documentation.html.text
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

object DependencyDocBuilder : DocumentationBuilder<StitcherAssignment> {
    // TODO: store implicit receiver in the tree
    override fun applyTo(builder: StringBuilder, element: StitcherAssignment) = html(builder) {
        val name = element.dependency?.text.orEmpty()
        val node = element.stonecutterNode
        val value = node?.params?.dependencies?.get(name)?.toString()
        signature("Dependency", "condition-dependencies", name.ifEmpty { "minecraft" }, value, AttributeKeys.DEPENDENCY, AttributeKeys.NUMBER)

        if (node == null || name.isEmpty()) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.dependencies[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            if (it == null) text("UNDEFINED", RED)
            else text(it.toString(), AttributeKeys.NUMBER)
        }
    }
}