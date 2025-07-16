package dev.kikugie.stonecutter.intellij.lang.access

/***
 * Determines the enclosing type of [ScopeDefinition].
 * - [ScopeType.OPENER]: `? ... ({|>>)`
 * - [ScopeType.EXTENSION]: `?} ... ({|>>)`
 * - [ScopeType.CLOSER]: `?}`
 * - [ScopeType.INVALID]: the comment syntax is invalid.
 */
enum class ScopeType {
    OPENER, EXTENSION, CLOSER, INVALID;
}

/**
 * Determines the region [ScopeDefinition] affects.
 * - [OpenerType.LINE]: the next line will be affected.
 * - [OpenerType.WORD]: the next sequence of non-whitespace characters will be affected.
 * - [OpenerType.OPEN]: everything until a [ScopeType.EXTENSION] or [ScopeType.CLOSER] comment will be affected.
 */
enum class OpenerType {
    LINE, WORD, OPEN
}