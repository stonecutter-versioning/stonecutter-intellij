package dev.kikugie.stonecutter.intellij.editor.documentation

import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.cell
import dev.kikugie.stonecutter.intellij.editor.documentation.html.html
import dev.kikugie.stonecutter.intellij.editor.documentation.html.text
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

object ConstantDocBuilder : DocumentationBuilder<PsiExpression.Constant> {
    override fun applyTo(builder: StringBuilder, element: PsiExpression.Constant) = html(builder) {
        val name = element.text
        val node = element.stonecutterNode
        signature("Constant", "condition-constants", name, AttributeKeys.CONSTANT, node)

        if (node == null) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.constants[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            cell {
                if (it == null) text("UNDEFINED", RED)
                else text(it.toString(), AttributeKeys.KEYWORD)
            }
        }
    }
}