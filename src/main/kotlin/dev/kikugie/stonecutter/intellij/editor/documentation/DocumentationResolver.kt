package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.psi.PsiElement
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherConstant
import dev.kikugie.stonecutter.intellij.lang.psi.StitcherAssignment
import dev.kikugie.stonecutter.intellij.model.SCProcessProperties

private const val WIKI_LINK = "https://stonecutter.kikugie.dev/wiki/config/params"

private fun StringBuilder.appendUnresolved() =
    appendStyled("UNRESOLVED", AttributeKeys.UNRESOLVED)

private fun StringBuilder.appendStyled(text: String, key: TextAttributesKey?) {
    if (key == null) append(text)
    else HtmlSyntaxInfoUtil.appendStyledSpan(this, key, text, 1F)
}

interface DocumentationResolver<T : PsiElement> {
    fun generate(builder: StringBuilder, scope: T, properties: SCProcessProperties?) = with(builder) {
        appendTitle()
        append(' ')
        appendSignature(scope)
        append(' ')
        appendStyled("is", AttributeKeys.KEYWORD)
        append(' ')
        if (properties == null) appendUnresolved()
        else appendValue(scope, properties)
    }

    fun StringBuilder.appendTitle()
    fun StringBuilder.appendSignature(element: T)
    fun StringBuilder.appendValue(element: T, properties: SCProcessProperties)

    object Constant : DocumentationResolver<StitcherConstant> {
        override fun StringBuilder.appendTitle() {
            val link = "$WIKI_LINK#condition-constants"
            append("<a href=\"$link\">Constant</a>")
        }

        override fun StringBuilder.appendSignature(element: StitcherConstant) {
            appendStyled(element.text, AttributeKeys.CONSTANT)
        }

        override fun StringBuilder.appendValue(element: StitcherConstant, properties: SCProcessProperties) {
            val value = properties.constants[element.text] ?: return appendUnresolved()
            appendStyled(value.toString(), AttributeKeys.KEYWORD)
        }
    }

    object Dependency : DocumentationResolver<StitcherAssignment> {
        override fun StringBuilder.appendTitle() {
            val link = "$WIKI_LINK#condition-dependencies"
            append("<a href=\"$link\">Dependency</a>")
        }

        override fun StringBuilder.appendSignature(element: StitcherAssignment) {
            val name = element.dependency?.text ?: "minecraft" // TODO: store implicit receiver in the tree
            appendStyled(name, AttributeKeys.DEPENDENCY)
        }

        override fun StringBuilder.appendValue(element: StitcherAssignment, properties: SCProcessProperties) {
            val value = properties.dependencies[element.dependency?.text.orEmpty()] ?: return appendUnresolved()
            appendStyled(value.toString(), AttributeKeys.NUMBER)
        }
    }
}