package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.options.BoundCompositeConfigurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import dev.kikugie.stonecutter.intellij.settings.FoldingOptions.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.Companion.STATE
import dev.kikugie.stonecutter.intellij.settings.config.PropertyConfigurable
import dev.kikugie.stonecutter.intellij.settings.config.checkbox
import dev.kikugie.stonecutter.intellij.settings.config.link
import dev.kikugie.stonecutter.intellij.settings.config.selector
import dev.kikugie.stonecutter.intellij.settings.config.comment

class StonecutterOptions : BoundCompositeConfigurable<PropertyConfigurable>("Stonecutter Dev", null) {
    class EditorOptions : PropertyConfigurable("Editor") {
        init {
            checkbox("Use custom import optimiser", STATE::useImportOptimizer)
            checkbox("Lock generated files", STATE::lockGeneratedFiles)

            selector("Code folding mode", FoldingMode.entries.toTypedArray(), STATE::foldDisabledBlocks)
            checkbox("Link folded regions", STATE::linkDisabledBlocks)

            comment("") // Line Break

            link("Configure Syntax Coloring") { ShowSettingsUtil.getInstance().showSettingsDialog(
                    ProjectManager.getInstance().defaultProject,
                    "Stonecutter") // Should match ColorSettingsPage displayName. Should be different from options Display name
            }

            comment(" Or go to: Settings → Editor → Color Scheme → Stonecutter")
        }
    }

    override fun createConfigurables(): List<PropertyConfigurable> = listOf(
        EditorOptions()
    )

    override fun createPanel(): DialogPanel = panel {
        for (it in configurables) appendDslConfigurable(it)
    }
}