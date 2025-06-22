package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.options.BoundCompositeConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import dev.kikugie.stonecutter.intellij.settings.FoldingOptions.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.Companion.STATE
import dev.kikugie.stonecutter.intellij.settings.config.PropertyConfigurable
import dev.kikugie.stonecutter.intellij.settings.config.checkbox
import dev.kikugie.stonecutter.intellij.settings.config.selector

class StonecutterOptions : BoundCompositeConfigurable<PropertyConfigurable>("Stonecutter", null) {
    class EditorOptions : PropertyConfigurable("Editor") {
        init {
            checkbox("Use custom import optimiser", STATE::useImportOptimizer)
            selector("Code folding mode", FoldingMode.entries.toTypedArray(), STATE::foldDisabledBlocks)
        }
    }

    override fun createConfigurables(): List<PropertyConfigurable> = listOf(
        EditorOptions()
    )

    override fun createPanel(): DialogPanel = panel {
        for (it in configurables) appendDslConfigurable(it)
    }
}