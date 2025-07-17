@file:Suppress("NOTHING_TO_INLINE", "unused")

package dev.kikugie.stonecutter.intellij.editor.documentation.html

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.richcopy.HtmlSyntaxInfoUtil
import com.intellij.openapi.util.text.StringUtil
import dev.kikugie.stonecutter.intellij.editor.documentation.StitcherDocumentationTarget
import kotlin.text.isEmpty

@DslMarker
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class HtmlDSL

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

private fun HtmlBuilder.joinArgs(prefix: String, suffix: String, vararg args: Pair<String, String>) {
    builder.append(prefix)
    for ((k, v) in args)
        builder.append(" $k=\"$v\"")
    builder.append(suffix)
}

fun HtmlBuilder.opener(tag: String, vararg args: Pair<String, String>) =
    joinArgs("<$tag", ">", *args)

fun HtmlBuilder.closer(tag: String) = raw("</$tag>")

fun HtmlBuilder.tag(tag: String, vararg args: Pair<String, String>) =
    joinArgs("<$tag", "/>", *args)

inline fun <T> T.tag(tag: String, provider: T.() -> Unit, vararg args: Pair<String, String>) where T : HtmlBuilder {
    opener(tag, *args)
    provider()
    closer(tag)
}

fun HtmlBuilder.nbsp() = raw(NBSP)
fun HtmlBuilder.nbsp(count: Int) = raw(NBSP.repeat(count))
fun HtmlBuilder.br() = tag("br")
fun HtmlBuilder.hr() = tag("hr")


fun HtmlBuilder.header(text: String, level: Int = 1) = header(level) { text(text) }
inline fun HtmlBuilder.header(level: Int = 1, provider: HtmlBuilder.() -> Unit) =
    tag("h$level", provider = provider)

fun HtmlBuilder.icon(name: String, source: String = StitcherDocumentationTarget.ICONS_FQN) =
    tag("icon", "src" to "$source.$name")

fun HtmlBuilder.link(url: String, text: String) = link(url) { text(text) }
inline fun HtmlBuilder.link(url: String, provider: HtmlBuilder.() -> Unit) =
    tag("a", provider, "href" to url)

inline fun HtmlBuilder.div(vararg args: Pair<String, String>, provider: HtmlBuilder.() -> Unit) =
    tag("div", provider, *args)

inline fun HtmlBuilder.span(vararg args: Pair<String, String>, provider: HtmlBuilder.() -> Unit) =
    tag("span", provider, *args)

inline fun HtmlBuilder.table(vararg args: Pair<String, String>, provider: TableHtmlBuilder.() -> Unit) {
    TableHtmlBuilder(builder).tag("table", provider, *args)
}

@JvmInline
value class TableHtmlBuilder(override val builder: StringBuilder) : HtmlBuilder

inline fun TableHtmlBuilder.row(vararg args: Pair<String, String>, provider: RowHtmlBuilder.() -> Unit) {
    RowHtmlBuilder(builder).tag("tr", provider, *args)
}

@JvmInline
value class RowHtmlBuilder(override val builder: StringBuilder) : HtmlBuilder

inline fun RowHtmlBuilder.cell(vararg args: Pair<String, String>, provider: HtmlBuilder.() -> Unit) =
    tag("td", provider, *args)

inline fun HtmlBuilder.list(provider: ListHtmlBuilder.() -> Unit) {
    ListHtmlBuilder(builder).tag("ol", provider)
}

inline fun HtmlBuilder.collection(provider: ListHtmlBuilder.() -> Unit) {
    ListHtmlBuilder(builder).tag("ul", provider)
}

@JvmInline
value class ListHtmlBuilder(override val builder: StringBuilder) : HtmlBuilder

inline fun ListHtmlBuilder.entry(provider: HtmlBuilder.() -> Unit) =
    tag("li", provider)
