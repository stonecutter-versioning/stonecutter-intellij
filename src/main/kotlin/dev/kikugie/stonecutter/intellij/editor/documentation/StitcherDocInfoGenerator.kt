package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafElement
import com.intellij.psi.util.descendants
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.Attribute
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties

object StitcherDocInfoGenerator {
    private const val WIKI_LINK = "https://stonecutter.kikugie.dev/wiki/config/params"
    private const val WIKI_CONSTANTS = "condition-constants"
    private const val WIKI_DEPENDENCIES = "condition-dependencies"

    private const val CONSTANT_TITLE = "Constant"
    private const val DEPENDENCY_TITLE = "Dependency"
    private const val UNRESOLVED_TITLE = "UNRESOLVED"

    fun highlight(builder: StringBuilder, scope: PsiElement, properties: SCProcessProperties?) = with(builder) {
        appendType(scope)
        append(' ')
        appendSignature(scope)
        append(' ')
        appendStyled("is", AttributeKeys.KEYWORD)
        append(' ')
        appendValue(scope, properties)
    }

    private fun StringBuilder.appendType(element: PsiElement) = when (element) {
        is StitcherConstant -> appendLink(element, CONSTANT_TITLE)
        is StitcherAssignment -> appendLink(element, DEPENDENCY_TITLE)
        else -> unsupported(element)
    }

    private fun StringBuilder.appendLink(element: PsiElement, title: String) {
        val link = "$WIKI_LINK#${category(element)}"
        append("<a href=\"$link\">$title</a>")
    }

    private fun category(element: PsiElement): String = when (element) {
        is StitcherConstant -> WIKI_CONSTANTS
        is StitcherAssignment -> WIKI_DEPENDENCIES
        else -> unsupported(element)
    }

    private fun StringBuilder.appendSignature(element: PsiElement) {
        for (it in element.descendants(true).filterIsInstance<LeafElement>())
            appendStyled(it.text, Attribute(it.elementType))
    }

    private fun StringBuilder.appendValue(element: PsiElement, properties: SCProcessProperties?) = when {
        properties == null -> appendUnresolved()
        element is StitcherConstant -> appendValue(element.text, properties.constants, AttributeKeys.KEYWORD)
        element is StitcherAssignment -> appendValue(element.dependency?.text.orEmpty(), properties.dependencies, AttributeKeys.NUMBER)
        else -> unsupported(element)
    }

    private fun StringBuilder.appendValue(text: String, lookup: Map<String, Any>, style: TextAttributesKey) {
        val value = lookup[text]?.toString() ?: return appendUnresolved()
        appendStyled(value, style)
    }

    private fun StringBuilder.appendUnresolved() = appendStyled(UNRESOLVED_TITLE, AttributeKeys.UNRESOLVED)

    private fun StringBuilder.appendStyled(text: String, key: TextAttributesKey?) {
        if (key == null) append(text)
        else HtmlSyntaxInfoUtil.appendStyledSpan(this, key, text, 1F)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun unsupported(element: PsiElement): Nothing =
        error("Unsupported element type for $element")
}
