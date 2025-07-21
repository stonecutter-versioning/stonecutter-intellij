package dev.kikugie.stonecutter.intellij.editor.index

@JvmInline
value class StitcherIndexKey(val value: String) {
    constructor(type: Type, name: String) : this("${type.name}:$name")
    enum class Type {
        CONSTANT, DEPENDENCY, SWAP, REPLACEMENT
    }

    val type: Type get() = Type.valueOf(value.substringBefore(':'))
    val name: String get() = value.substringAfter(':', "")
}