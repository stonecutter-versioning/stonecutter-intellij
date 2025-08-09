package dev.kikugie.stonecutter.intellij.settings

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BoundCompositeConfigurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.Companion.STATE
import dev.kikugie.stonecutter.intellij.settings.config.PropertyConfigurable
import dev.kikugie.stonecutter.intellij.settings.config.buttonSelector
import dev.kikugie.stonecutter.intellij.settings.config.checkbox
import dev.kikugie.stonecutter.intellij.settings.config.link
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingStyle

class StonecutterOptions : BoundCompositeConfigurable<PropertyConfigurable>("Stonecutter Dev", null) {
    class FoldingOptions(title: String) : CodeFoldingOptionsProvider, PropertyConfigurable(title, {
        buttonSelector("Mode", FoldingMode.entries, STATE::foldDisabledBlocks)
        buttonSelector("Comment style", FoldingStyle.entries, STATE::foldedPresentation)
        checkbox("Link regions", STATE::linkDisabledBlocks)
            .tooltip("Fold Stonecutter regions together")
    }) {
        @Suppress("unused") /* EP constructor */ constructor() : this("Stonecutter")
    }

    class EditorOptions : PropertyConfigurable("Editor", {
        checkbox("Use custom import optimiser", STATE::useImportOptimizer)
        checkbox("Lock generated files", STATE::lockGeneratedFiles)
        checkbox("Refresh project after switch", STATE::refreshAfterSwitch)

        link("Configure Syntax Coloring") {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                ProjectManager.getInstance().defaultProject,
                "Stonecutter"
            ) // Should match ColorSettingsPage displayName. Should be different from options Display name
        }
    })

    override fun createConfigurables(): List<PropertyConfigurable> = listOf(
        EditorOptions(),
        FoldingOptions("Folding")
    )

    override fun createPanel(): DialogPanel = panel {
        for (it in configurables) appendDslConfigurable(it)
    }
}