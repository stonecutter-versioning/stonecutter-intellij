package dev.kikugie.stonecutter.intellij.settings

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BoundCompositeConfigurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import dev.kikugie.stonecutter.intellij.StonecutterBundle
import dev.kikugie.stonecutter.intellij.StonecutterBundle.message
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.Companion.STATE
import dev.kikugie.stonecutter.intellij.settings.config.PropertyConfigurable
import dev.kikugie.stonecutter.intellij.settings.config.buttonSelector
import dev.kikugie.stonecutter.intellij.settings.config.checkbox
import dev.kikugie.stonecutter.intellij.settings.config.link
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingStyle

class StonecutterOptions : BoundCompositeConfigurable<PropertyConfigurable>("Stonecutter Dev", null) {
    class FoldingOptions(title: String) : CodeFoldingOptionsProvider, PropertyConfigurable(title, {
        buttonSelector(message("stonecutter.settings.folding.mode"), FoldingMode.entries, STATE::foldDisabledBlocks)
        buttonSelector(message("stonecutter.settings.folding.style"), FoldingStyle.entries, STATE::foldedPresentation)
        checkbox(message("stonecutter.settings.folding.link"), STATE::linkDisabledBlocks)
    }) {
        @Suppress("unused") /* EP constructor */ constructor() : this("Stonecutter")
    }

    class EditorOptions : PropertyConfigurable(message("stonecutter.settings.editor"), {
//        checkbox(message("stonecutter.settings.editor.imports"), STATE::useImportOptimizer)
//            .tooltip(message("stonecutter.settings.editor.imports.tooltip"))
        checkbox(message("stonecutter.settings.editor.lock"), STATE::lockGeneratedFiles)
        checkbox(message("stonecutter.settings.editor.sync"), STATE::refreshAfterSwitch)

        link(message("stonecutter.settings.editor.colours")) {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                ProjectManager.getInstance().defaultProject,
                "Stonecutter"
            ) // Should match ColorSettingsPage displayName. Should be different from options Display name
        }
    })

    override fun createConfigurables(): List<PropertyConfigurable> = listOf(
        EditorOptions(),
//        FoldingOptions(message("stonecutter.settings.folding"))
    )

    override fun createPanel(): DialogPanel = panel {
        for (it in configurables) appendDslConfigurable(it)
    }
}