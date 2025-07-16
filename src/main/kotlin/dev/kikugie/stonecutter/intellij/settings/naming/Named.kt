package dev.kikugie.stonecutter.intellij.settings.naming

/**
 * Provides the implemented class with a human-readable [display] name
 * and optional [description].
 *
 * Instance names are checked at call sites with [display()][Any.display]
 * and [description()][Any.description].
 *
 * @see Supplied
 * @see Translated
 */
interface Named {
    val display: String
    val description: String? get() = null
}

