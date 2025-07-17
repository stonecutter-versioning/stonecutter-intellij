@file:Suppress("NOTHING_TO_INLINE")

package dev.kikugie.stonecutter.intellij.editor.documentation.html

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.util.text.StringUtil
import dev.kikugie.stonecutter.intellij.editor.documentation.StitcherDocumentationTarget

const val NBSP = "&nbsp;"

fun String.escapeHtml() = StringUtil
    .escapeXmlEntities(this)
    .replace("\n", "<br/>")

fun StringBuilder.appendStyled(text: String, style: TextAttributesKey) {
    HtmlSyntaxInfoUtil.appendStyledSpan(this, style, text, 1F)
}

fun StringBuilder.appendStyled(text: String, style: TextAttributes) {
    HtmlSyntaxInfoUtil.appendStyledSpan(this, style, text, 1F)
}

@JvmInline
value class RootHtmlBuilder(override val builder: StringBuilder) : HtmlBuilder

inline fun html(builder: StringBuilder, action: HtmlBuilder.() -> Unit) {
    RootHtmlBuilder(builder).action()
}

fun HtmlBuilder.text(text: String) {
    builder.append(text.escapeHtml())
}

fun HtmlBuilder.text(text: String, style: TextAttributesKey) =
    builder.appendStyled(text.escapeHtml(), style)

fun HtmlBuilder.text(text: String, style: TextAttributes) =
    builder.appendStyled(text.escapeHtml(), style)

fun HtmlBuilder.text(provider: StringBuilder.() -> Unit) =
    text(buildString(provider))

inline fun HtmlBuilder.text(style: TextAttributesKey, provider: StringBuilder.() -> Unit) =
    text(buildString(provider), style)

inline fun HtmlBuilder.text(style: TextAttributes, provider: StringBuilder.() -> Unit) =
    text(buildString(provider), style)

fun HtmlBuilder.raw(string: String) {
    builder.append(string)
}

inline fun HtmlBuilder.raw(provider: StringBuilder.() -> Unit) {
    builder.provider()
}

fun HtmlBuilder.single(tag: String, args: String = "") =
    if (args.isEmpty()) raw("<$tag/>") else raw("<$tag $args/>")
fun HtmlBuilder.opener(tag: String, args: String = "") =
    if (args.isEmpty()) raw("<$tag>") else raw("<$tag $args>")
fun HtmlBuilder.closer(tag: String) = raw("</$tag>")

fun HtmlBuilder.nbsp() = raw(NBSP)
fun HtmlBuilder.nbsp(count: Int) = raw(NBSP.repeat(count))
fun HtmlBuilder.br() = single("br")
fun HtmlBuilder.hr() = single("hr")

inline fun <T> T.wrap(tag: String, args: String = "", provider: T.() -> Unit) where T : HtmlBuilder {
    opener(tag, args)
    provider()
    closer(tag)
}

fun HtmlBuilder.icon(name: String, source: String = StitcherDocumentationTarget.ICONS_FQN) =
    single("icon", "src=\"$source.$name\"")

fun HtmlBuilder.link(url: String, text: String) = link(url) { text(text) }
inline fun HtmlBuilder.link(url: String, provider: HtmlBuilder.() -> Unit) =
    wrap("a", "href=\"$url\"", provider)

inline fun HtmlBuilder.div(provider: HtmlBuilder.() -> Unit) =
    wrap("div", "", provider)

inline fun HtmlBuilder.div(cls: String, provider: HtmlBuilder.() -> Unit) =
    wrap("div", "class='$cls'", provider)

inline fun HtmlBuilder.table(provider: TableHtmlBuilder.() -> Unit) {
    TableHtmlBuilder(builder).wrap("table", "", provider)
}

@JvmInline
value class TableHtmlBuilder(override val builder: StringBuilder) : HtmlBuilder

inline fun TableHtmlBuilder.row(provider: RowHtmlBuilder.() -> Unit) {
    RowHtmlBuilder(builder).wrap("tr", "", provider)
}

@JvmInline
value class RowHtmlBuilder(override val builder: StringBuilder) : HtmlBuilder

inline fun RowHtmlBuilder.cell(provider: HtmlBuilder.() -> Unit) =
    wrap("td", "", provider)