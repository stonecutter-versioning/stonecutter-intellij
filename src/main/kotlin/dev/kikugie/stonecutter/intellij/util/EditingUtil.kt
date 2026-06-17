package dev.kikugie.stonecutter.intellij.util

import com.intellij.openapi.editor.Document

internal fun Document.findIndentAt(offset: Int): String =
    findIndentAtLine(getLineNumber(offset))

internal fun Document.findIndentAtLine(line: Int): String {
    val start = getLineStartOffset(line)
    val end = getLineEndOffset(line)
    if (start == end) return ""

    for (i in start..<end) when (charsSequence[i]) {
        ' ', '\t' -> continue
        else -> return charsSequence.substring(start, i)
    }
    return charsSequence.substring(start, end)
}