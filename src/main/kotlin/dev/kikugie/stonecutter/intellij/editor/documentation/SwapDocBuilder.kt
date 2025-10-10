package dev.kikugie.stonecutter.intellij.editor.documentation

import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.cell
import dev.kikugie.stonecutter.intellij.editor.documentation.html.html
import dev.kikugie.stonecutter.intellij.editor.documentation.html.text
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as DefaultColors

object SwapDocBuilder : DocumentationBuilder<PsiSwap> {
    override fun applyTo(builder: StringBuilder, element: PsiSwap) = html(builder) {
        val name = element.text.orEmpty()
        val node = element.stonecutterNode
        signature("Swap", "string-swaps", name, AttributeKeys.SWAP, node)

        if (node == null || name.isEmpty()) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.swaps[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            cell {
                if (it == null) text("UNDEFINED", RED)
                else text(it.trimIndent(), DefaultColors.STRING)
            }
        }
    }
}