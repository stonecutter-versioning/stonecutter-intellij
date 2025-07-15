package dev.kikugie.stonecutter.intellij.settings.variants

import dev.kikugie.stonecutter.intellij.settings.naming.Named

enum class FoldingMode(override val display: String) : Named {
    DISABLED("Disabled"),
    LENIENT("Keep expanded"),
    AGGRESSIVE("Always collapse")
}

enum class FoldingStyle(override val display: String, override val description: String) : Named {
    KEEP_COMMENTS("Keep markers", "'//? line' '/*? block */'"),
    HIDE_INLINE("Hide block markers", "'//? line' '? block'"),
    HIDE_ALL("Hide all markers", "'? line' '? block'")
}