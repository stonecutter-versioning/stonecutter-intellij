package dev.kikugie.stonecutter.intellij.util

inline fun Nothing?.also(action: () -> Unit): Nothing? {
    action()
    return this
}

inline fun Boolean.whenIt(action: (Boolean) -> Unit): Boolean = also { if (this) action(true) }
inline fun Boolean.whenNot(action: (Boolean) -> Unit): Boolean = also { if (!this) action(false) }
inline fun <T> catchingLazy(crossinline provider: () -> T): Lazy<Result<T>> = lazy { runCatching { provider() } }
inline fun <T, R> Result<T>.mapResult(mapping: (T) -> Result<R>): Result<R> =
    map { mapping(it) }.getOrElse { Result.failure(it) }