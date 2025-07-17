package dev.kikugie.stonecutter.intellij.editor.documentation

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.JBColor
import dev.kikugie.stonecutter.intellij.editor.StitcherSyntaxHighlighter.AttributeKeys
import dev.kikugie.stonecutter.intellij.editor.documentation.html.*
import dev.kikugie.stonecutter.intellij.model.SCProjectNode

internal val RED = TextAttributes().apply {
    effectColor = JBColor.RED
}

internal fun HtmlBuilder.signature(title: String, section: String, name: String, style: TextAttributesKey, marker: Any?) =
    header(5) {
        link("https://stonecutter.kikugie.dev/wiki/config/params#$section", title)
        nbsp()
        tag("code", { text(name, style) })
        nbsp()
        if (marker == null) {
            text("is", AttributeKeys.KEYWORD)
            nbsp()
            text("UNRESOLVED", RED)
        }
    }

internal fun <T> HtmlBuilder.variants(current: SCProjectNode, variants: Map<T?, List<SCProjectNode>>, value: RowHtmlBuilder.(T?) -> Unit) =
    div("class" to "bottom") {
        table {
            for ((value, nodes) in variants) row("valign" to "top") {
                cell { icon(if (current in nodes) "VERSION_ENTRY" else "VERSION_EMPTY") }
                value(value)
                cell { text("in", AttributeKeys.KEYWORD) }
                cell { raw { nodes.joinTo(this) { it.metadata.project } } }
            }
        }
    }