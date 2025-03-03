package dev.kikugie.stonecutter.intellij.util

inline fun <T, R> Result<T>.mapResult(mapping: (T) -> Result<R>): Result<R> =
    map { mapping(it) }.getOrElse { Result.failure(it) }

fun <T> invalidArg(message: String): Result<T> = Result.failure(IllegalArgumentException(message))
fun <T> invalidState(message: String): Result<T> = Result.failure(IllegalStateException(message))

inline fun <T> requireNotNullResult(value: T?, message: () -> String): Result<T> =
    if (value != null) Result.success(value) else invalidArg(message())

inline fun <T> checkNotNullResult(value: T?, message: () -> String): Result<T> =
    if (value != null) Result.success(value) else invalidState(message())

inline fun <T, R> Result<T>.lazyMapping(crossinline mapping: (T) -> R): Lazy<Result<R>> = lazy {
    map { mapping(it) }
}

inline fun <T, R> Result<T>.lazyCatchMapping(crossinline mapping: (T) -> R): Lazy<Result<R>> = lazy {
    mapCatching { mapping(it) }
}

inline fun <T, R> Result<T>.lazyResultMapping(crossinline mapping: (T) -> Result<R>): Lazy<Result<R>> = lazy {
    mapResult { mapping(it) }
}

inline fun <T, R> (() -> Result<T>).lazyMapping(crossinline mapping: (T) -> R): Lazy<Result<R>> = lazy {
    invoke().map { mapping(it) }
}

inline fun <T, R> (() -> Result<T>).lazyCatchMapping(crossinline mapping: (T) -> R): Lazy<Result<R>> = lazy {
    invoke().mapCatching { mapping(it) }
}

inline fun <T, R> (() -> Result<T>).lazyResultMapping(crossinline mapping: (T) -> Result<R>): Lazy<Result<R>> = lazy {
    invoke().mapResult { mapping(it) }
}