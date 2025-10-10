package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.lang.Language

import com.intellij.openapi.project.Project
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.*
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.model.serialized.RegexReplacement
import dev.kikugie.stonecutter.intellij.model.serialized.Replacement
import dev.kikugie.stonecutter.intellij.model.serialized.StringReplacement
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

// WTF IntelliJ?!
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as DefaultColors
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet as appendHighlighted

private fun RowHtmlBuilder.replacement(project: Project, replacement: Replacement) = when (replacement) {
    is StringReplacement -> stringReplacement(replacement)
    is RegexReplacement -> regexReplacement(project, replacement)
}

private fun RowHtmlBuilder.stringReplacement(replacement: StringReplacement) {
    cell {
        for (it in replacement.sources) {
            text("•")
            nbsp()
            text(it, DefaultColors.STRING)
            br()
        }
    }
    cell { text("→", AttributeKeys.KEYWORD) }
    cell { text(replacement.target, DefaultColors.STRING) }
}

private fun RowHtmlBuilder.regexReplacement(project: Project, replacement: RegexReplacement) {
    cell {
        val language = Language.findLanguageByID("RegExp")
        if (language == null) text(replacement.pattern, DefaultColors.STRING)
        else raw { appendHighlighted(this, project, language, replacement.pattern, 1F) }
    }
    cell { text("→", AttributeKeys.KEYWORD) }
    cell { text(replacement.target, DefaultColors.STRING) }
}

object ReplacementDocBuilder : DocumentationBuilder<PsiReplacement> {
    override fun applyTo(builder: StringBuilder, element: PsiReplacement) = html(builder) {
        val name = element.text
        val node = element.stonecutterNode
        signature("Replacement", "replacements", name, AttributeKeys.REPLACEMENT, node)

        if (node == null) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.replacements[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            if (it == null) cell { text("UNDEFINED", RED) }
            else replacement(element.project, it)
        }
    }
}