package dev.kikugie.stonecutter.intellij.lang.impl

fun String.findTemplates(): Sequence<TemplatePlace> {
    val lexer = SwapTemplate(null).apply { reset(this@findTemplates, 0, this@findTemplates.length, 0) }
    return generateSequence {
        if (lexer.advance() == null) return@generateSequence null
        val number = substring(lexer.tokenStart + 1, lexer.tokenEnd).toIntOrNull() ?: return@generateSequence null
        TemplatePlace(number, lexer.tokenStart, lexer.tokenEnd)
    }
}

data class TemplatePlace(val number: Int, val start: Int, val end: Int) {
    fun apply(builder: StringBuilder, value: String, offset: Int): Int {
        builder.replace(start + offset, end + offset, value)
        return (end - start) - value.length
    }
}