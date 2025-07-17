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

internal fun HtmlBuilder.signature(title: String, section: String, name: String, value: String?, vararg styles: TextAttributesKey) =
    div("definition") {
        link("https://stonecutter.kikugie.dev/wiki/config/params#$section", title)
        nbsp()
        text(name, styles[0])
        nbsp()
        text("is", AttributeKeys.KEYWORD)
        nbsp()
        raw {
            if (value == null) appendStyled("UNRESOLVED", RED)
            else appendStyled(value, styles[1])
        }
    }

internal fun <T> HtmlBuilder.variants(current: SCProjectNode, variants: Map<T?, List<SCProjectNode>>, mapper: HtmlBuilder.(T?) -> Unit) =
    div("bottom") {
        table {
            for ((value, nodes) in variants) row {
                cell { icon(if (current in nodes) "VERSION_ENTRY" else "VERSION_EMPTY") }
                cell { mapper(value) }
                cell { text("in", AttributeKeys.KEYWORD) }
                cell { raw { nodes.joinTo(this) { it.metadata.project } } }
            }
        }
    }