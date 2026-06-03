package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import dev.kikugie.commons.takeAsOrNull
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.ReplacementToggleDocBuilder.replacement
import dev.kikugie.stonecutter.intellij.editor.documentation.html.*
import dev.kikugie.stonecutter.intellij.lang.impl.ExpressionEvaluationVisitor
import dev.kikugie.stonecutter.intellij.lang.impl.findTemplates
import dev.kikugie.stonecutter.intellij.lang.psi.PsiExpression
import dev.kikugie.stonecutter.intellij.lang.psi.PsiReplacement
import dev.kikugie.stonecutter.intellij.lang.psi.PsiSwap
import dev.kikugie.stonecutter.intellij.lang.util.unquote
import dev.kikugie.stonecutter.intellij.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.model.serialized.PerlReplacement
import dev.kikugie.stonecutter.intellij.model.serialized.RegexReplacement
import dev.kikugie.stonecutter.intellij.model.serialized.Replacement
import dev.kikugie.stonecutter.intellij.model.serialized.StringReplacement
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService
import kotlin.text.trimIndent
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as DefaultColors
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil.appendHighlightedByLexerAndEncodedAsHtmlCodeSnippet as appendHighlighted


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

object SwapIdDocBuilder : DocumentationBuilder<PsiSwap.Opener> {
    override fun applyTo(builder: StringBuilder, element: PsiSwap.Opener) = html(builder) {
        val name = element.identifier?.text.orEmpty()
        val node = element.stonecutterNode
        signature("Swap", "string-swaps", name, AttributeKeys.SWAP, node)

        if (node == null || name.isEmpty()) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.params.swaps[name] }
        if (variants.isNotEmpty()) variants(node, variants) {
            cell {
                if (it == null) text("UNDEFINED", RED)
                else buildStringTemplate(it.trimIndent(), element)
            }
        }
    }

    private fun HtmlBuilder.buildStringTemplate(str: String, element: PsiSwap.Opener) {
        val params = element.args.map(PsiElement::unquote).toList()
        var lastEnd = 0
        for (place in str.findTemplates()) {
            val substitution = params.getOrNull(place.number - 1)

            text(str.substring(lastEnd, place.start), DefaultColors.STRING)
            if (substitution == null) text(str.substring(place.start, place.end), RED)
            else text(substitution, DefaultColors.PARAMETER)
            lastEnd = place.end
        }
        if (lastEnd == 0) text(str, DefaultColors.STRING)
        else if (lastEnd < str.length) text(str.substring(lastEnd), DefaultColors.STRING)
    }
}

object SwapLocalDocBuilder : DocumentationBuilder<PsiSwap.Local> {
    override fun applyTo(builder: StringBuilder, element: PsiSwap.Local) = html(builder) {
        val node = element.stonecutterNode
        signature("Local swap", "local-swaps", null, AttributeKeys.DEPENDENCY, node)

        if (node == null) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.eval(element) }
        if (variants.isNotEmpty()) variants(node, variants) {
            cell {
                if (it == null) text("UNDEFINED", RED)
                else text(it, DefaultColors.STRING)
            }
        }
    }

    private fun SCProjectNode.eval(element: PsiSwap.Local): String? {
        val visitor = ExpressionEvaluationVisitor(this)
        val variant = element.entries.find { it.condition?.accept(visitor) ?: true }
            ?: return null
        return variant.literal?.unquote()
    }
}

object DependencyDocBuilder : DocumentationBuilder<PsiExpression.Assignment> {
    override fun applyTo(builder: StringBuilder, element: PsiExpression.Assignment) = html(builder) {
        val name = element.target?.text.orEmpty()
        val node = element.stonecutterNode
        signature("Dependency", "condition-dependencies", name.ifEmpty { "minecraft" }, AttributeKeys.DEPENDENCY, node)

        if (node == null) return@html
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

object ReplacementToggleDocBuilder : DocumentationBuilder<PsiReplacement.Toggle.Entry> {
    override fun applyTo(builder: StringBuilder, element: PsiReplacement.Toggle.Entry) = html(builder) {
        val name = element.lastChild.text
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

    internal fun RowHtmlBuilder.replacement(project: Project, replacement: Replacement): Unit = when (replacement) {
        is StringReplacement -> stringReplacement(replacement)
        is RegexReplacement -> regexReplacement(project, replacement)
        is PerlReplacement -> perlReplacement(replacement)
    }

    private fun RowHtmlBuilder.stringReplacement(replacement: StringReplacement) {
        val patterns = replacement.sources.toMutableList()
        if (replacement.pattern != null) patterns += replacement.pattern

        cell {
            for (it in patterns) {
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
        val pattern = replacement.regex?.pattern ?: replacement.pattern ?: "<unknown>"
        cell {
            val language = Language.findLanguageByID("RegExp")
            if (language == null) text(pattern, DefaultColors.STRING)
            else raw { appendHighlighted(this, project, language, pattern, 1F) }
        }
        cell { text("→", AttributeKeys.KEYWORD) }
        cell { text(replacement.target, DefaultColors.STRING) }
    }

    private fun RowHtmlBuilder.perlReplacement(replacement: PerlReplacement) {
        cell {
            text(replacement.pattern, DefaultColors.STRING)
        }
        cell { text("→", AttributeKeys.KEYWORD) }
        cell { text(replacement.target, DefaultColors.STRING) }
    }
}

object ReplacementLocalDocBuild : DocumentationBuilder<PsiReplacement.Local.Entry> {
    override fun applyTo(builder: StringBuilder, element: PsiReplacement.Local.Entry) = html(builder) {
        val node = element.stonecutterNode
        signature("Local replacement", "local-replacements", null, AttributeKeys.REPLACEMENT, node)

        if (node == null) return@html
        val variants = node.siblings(element.stonecutterService.lookup)
            .groupBy { it.eval(element) }
        if (variants.isNotEmpty()) variants(node, variants) {
            if (it == null) cell { text("UNDEFINED", RED) }
            else replacement(element.project, it)
        }
    }

    private fun SCProjectNode.eval(element: PsiReplacement.Local.Entry): StringReplacement? {
        val direction = element.parent.takeAsOrNull<PsiReplacement.Local>()?.condition?.accept(ExpressionEvaluationVisitor(this))
            ?: return null
        val source = element.source?.unquote().orEmpty()
        val target = element.target?.unquote().orEmpty()

        return if (direction) StringReplacement(pattern = source, target = target)
        else StringReplacement(pattern = target, target = source)
    }
}

