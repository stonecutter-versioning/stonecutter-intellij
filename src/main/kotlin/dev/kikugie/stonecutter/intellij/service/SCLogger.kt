package dev.kikugie.stonecutter.intellij.service

import com.intellij.openapi.diagnostic.Logger
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.writeText

class SCLogger(val name: String, val file: Path) : Logger() {
    override fun isDebugEnabled(): Boolean = true

    override fun debug(message: String, error: Throwable?) = appendString {
        appendLine("DEBUG[$name]: $message")
        error?.stackTraceToString()?.let(::appendLine)
    }

    override fun info(message: String, error: Throwable?) = appendString {
        appendLine("INFO[$name]: $message")
        error?.stackTraceToString()?.let(::appendLine)
    }

    override fun warn(message: String, error: Throwable?) = appendString {
        appendLine("WARN[$name]: $message")
        error?.stackTraceToString()?.let(::appendLine)
    }

    override fun error(message: String, error: Throwable?, vararg details: String) = appendString {
        appendLine("ERROR[$name]: $message")
        if (details.isNotEmpty()) appendLine("Details:\n${details.joinToString("\n")}")
        error?.stackTraceToString()?.let(::appendLine)
    }

    private inline fun appendString(action: StringBuilder.() -> Unit) = append(buildString(action))

    private fun append(text: String) = synchronized(this) {
        file.writeText(text + "\n", Charsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)
    }
}