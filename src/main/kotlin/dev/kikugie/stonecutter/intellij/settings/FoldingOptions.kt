package dev.kikugie.stonecutter.intellij.settings

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.Companion.STATE
import dev.kikugie.stonecutter.intellij.settings.config.PropertyConfigurable
import dev.kikugie.stonecutter.intellij.settings.config.checkbox
import dev.kikugie.stonecutter.intellij.settings.config.selector

class FoldingOptions : PropertyConfigurable("Stonecutter"), CodeFoldingOptionsProvider {
    enum class FoldingMode {
        DISABLED, LENIENT, AGGRESSIVE
    }

    init {
        selector("Code folding mode", FoldingMode.entries.toTypedArray(), STATE::foldDisabledBlocks)
        checkbox("Link folded regions", STATE::linkDisabledBlocks)
    }
}