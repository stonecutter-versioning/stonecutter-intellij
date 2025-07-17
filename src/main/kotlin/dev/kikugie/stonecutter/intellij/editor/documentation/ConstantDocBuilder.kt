package dev.kikugie.stonecutter.intellij.editor.documentation

import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.html
import dev.kikugie.stonecutter.intellij.editor.documentation.html.text
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

object ConstantDocBuilder : DocumentationBuilder<StitcherConstant> {
    override fun applyTo(builder: StringBuilder, element: StitcherConstant) = html(builder) {
        val name = element.text
        val node = element.stonecutterNode
        val value = node?.params?.constants?.get(name)?.toString()
        signature("Constant", "condition-constants", name, value, AttributeKeys.CONSTANT, AttributeKeys.KEYWORD)

        if (node == null) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.constants[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            if (it == null) text("UNDEFINED", RED)
            else text(it.toString(), AttributeKeys.KEYWORD)
        }
    }
}