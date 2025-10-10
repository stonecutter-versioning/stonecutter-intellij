package dev.kikugie.stonecutter.intellij.editor.documentation

import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.cell
import dev.kikugie.stonecutter.intellij.editor.documentation.html.html
import dev.kikugie.stonecutter.intellij.editor.documentation.html.text
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

object DependencyDocBuilder : DocumentationBuilder<PsiExpression.Assignment> {
    // TODO: store implicit receiver in the tree
    override fun applyTo(builder: StringBuilder, element: PsiExpression.Assignment) = html(builder) {
        val name = element.target?.text.orEmpty()
        val node = element.stonecutterNode
        signature("Dependency", "condition-dependencies", name.ifEmpty { "minecraft" }, AttributeKeys.DEPENDENCY, node)

        if (node == null || name.isEmpty()) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.dependencies[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            cell {
                if (it == null) text("UNDEFINED", RED)
                else text(it.value, AttributeKeys.VERSION)
            }
        }
    }
}