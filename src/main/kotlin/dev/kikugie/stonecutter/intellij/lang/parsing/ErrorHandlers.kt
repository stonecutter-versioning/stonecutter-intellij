package dev.kikugie.stonecutter.intellij.lang.parsing

import dev.kikugie.stonecutter.intellij.lang.token.StitcherType
import java.util.concurrent.ConcurrentHashMap

class ErrorHandlers(private val factory: (StitcherType?) -> Handler) {
    private val handlers: MutableMap<String, Handler> = ConcurrentHashMap()

    operator fun contains(name: String) = name in handlers

    fun handle(name: String, type: StitcherType? = null, msg: String = "") = handlers
        .getOrPut(name) { factory(type) }.mark(msg)
    fun releaseAll() = handlers.values.forEach { it.release() }.also { handlers.clear() }
    fun release(vararg names: String) = names.forEach { handlers.remove(it)?.release()}

    interface Handler {
        fun mark(message: String)
        fun release()
    }
}