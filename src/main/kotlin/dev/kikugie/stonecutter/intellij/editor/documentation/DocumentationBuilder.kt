package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.model.SCProjectNode
import dev.kikugie.stonecutter.intellij.service.StonecutterModelLookup
import dev.kikugie.stonecutter.intellij.service.stonecutterNode
import dev.kikugie.stonecutter.intellij.service.stonecutterService

internal const val NBSP = "&nbsp;"

internal fun StringBuilder.appendStonecutterIcon(name: String) {
    append("<icon src=\"${StitcherDocumentationTarget.ICONS_FQN}.$name\"/>")
}

internal fun StringBuilder.appendStyled(text: String, key: TextAttributesKey?) {
    if (key == null) append(text)
    else HtmlSyntaxInfoUtil.appendStyledSpan(this, key, text, 1F)
}

private fun StringBuilder.appendSignature(title: String, section: String, name: String, value: String?, vararg styles: TextAttributesKey) {
    append(DocumentationMarkup.DEFINITION_START)
    append("<a href=\"https://stonecutter.kikugie.dev/wiki/config/params#$section\">$title</a>")
    append(NBSP)
    appendStyled(name, styles[0])
    append(NBSP)
    appendStyled("is", AttributeKeys.KEYWORD)
    append(NBSP)
    if (value == null) appendStyled("UNRESOLVED", AttributeKeys.UNRESOLVED)
    else appendStyled(value, styles[1])
    append(DocumentationMarkup.DEFINITION_END)
}

private fun StonecutterModelLookup.getAllSiblings(node: SCProjectNode): Sequence<SCProjectNode> {
    val tree = branches[node.branch]?.tree?.let(trees::get)
        ?: return emptySequence()
    return tree.branches.asSequence()
        .mapNotNull(branches::get)
        .flatMap { it.nodes.asSequence().mapNotNull(nodes::get) }
}

private fun <T> StringBuilder.appendVariants(current: SCProjectNode, variants: Map<T?, List<SCProjectNode>>, mapper: StringBuilder.(T?) -> Unit) {
    if (variants.isEmpty()) return
    append("<div class=\"bottom\">")
    append(DocumentationMarkup.SECTIONS_START)
    val identifier = current.hierarchy.trim()
    for ((value, nodes) in variants) {
        val targets = buildSet { nodes.mapTo(this) { it.hierarchy.trim() } }
        append("<tr><td>")
        appendStonecutterIcon(if (identifier in targets) "VERSION_ENTRY" else "VERSION_EMPTY")
        append("</td><td>")
        mapper(value)
        append("</td><td>")
        appendStyled("in", AttributeKeys.KEYWORD)
        append("</td><td>")
        targets.joinTo(this)
        append("</td></tr>")
    }
    append(DocumentationMarkup.DEFINITION_END)
    append("</div>")
}

interface DocumentationBuilder<T : PsiElement> {
    fun applyTo(builder: StringBuilder, element: T)
}

object ConstantDocBuilder : DocumentationBuilder<StitcherConstant> {
    override fun applyTo(builder: StringBuilder, element: StitcherConstant) = with(builder) {
        val name = element.text
        val node = element.stonecutterNode
        val value = node?.params?.constants?.get(name)?.toString()
        appendSignature("Constant", "condition-constants", name, value, AttributeKeys.CONSTANT, AttributeKeys.KEYWORD)

        if (node == null) return@with
        val variants = element.stonecutterService.lookup.getAllSiblings(node)
            .groupBy { it.params.constants[name] }
        appendVariants(node, variants) {
            if (it == null) appendStyled("UNDEFINED", AttributeKeys.UNRESOLVED)
            else appendStyled(it.toString(), AttributeKeys.KEYWORD)
        }
    }
}

object DependencyDocBuilder : DocumentationBuilder<StitcherAssignment> {
    // TODO: store implicit receiver in the tree
    override fun applyTo(builder: StringBuilder, element: StitcherAssignment) = with(builder) {
        val name = element.dependency?.text.orEmpty()
        val node = element.stonecutterNode
        val value = node?.params?.dependencies?.get(name)?.toString()
        appendSignature("Dependency", "condition-dependencies", name.ifEmpty { "minecraft" }, value, AttributeKeys.DEPENDENCY, AttributeKeys.NUMBER)

        if (node == null || name.isEmpty()) return@with
        val variants = element.stonecutterService.lookup.getAllSiblings(node)
            .groupBy { it.params.dependencies[name] }
        appendVariants(node, variants) {
            if (it == null) appendStyled("UNDEFINED", AttributeKeys.UNRESOLVED)
            else appendStyled(it.toString(), AttributeKeys.NUMBER)
        }
    }
}