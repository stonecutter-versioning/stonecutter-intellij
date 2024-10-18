package dev.kikugie.stonecutter.intellij.util

inline fun Nothing?.also(action: () -> Unit): Nothing? {
    action()
    return this
}

inline fun Boolean.whenIt(action: (Boolean) -> Unit): Boolean = also { if (this) action(true) }
inline fun Boolean.whenNot(action: (Boolean) -> Unit): Boolean = also { if (!this) action(false) }