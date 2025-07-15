package dev.kikugie.stonecutter.intellij.settings.variants

import dev.kikugie.stonecutter.intellij.settings.naming.Translated

enum class FoldingMode() : Translated {
    DISABLED,
    LENIENT,
    AGGRESSIVE;

    private val identifier = name.lowercase()
    override val namer: String
        get() = "stonecutter.settings.folding.mode.$identifier"
}

enum class FoldingStyle(override val description: String) : Translated {
    KEEP_COMMENTS("'//? line' '/*? block */'"),
    HIDE_INLINE("'//? line' '? block'"),
    HIDE_ALL("'? line' '? block'");

    private val identifier = name.lowercase()
    override val namer: String
        get() = "stonecutter.settings.folding.style.$identifier"
}