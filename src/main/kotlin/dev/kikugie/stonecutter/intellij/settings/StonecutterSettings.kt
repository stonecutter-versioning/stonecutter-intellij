package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import dev.kikugie.stonecutter.intellij.service.StonecutterCallbacks
import dev.kikugie.stonecutter.intellij.settings.FoldingOptions.FoldingMode

@Service(Service.Level.APP) @State(
    name = "dev.kikugie.stonecutter.intellij.StonecutterSettings",
    storages = [Storage("StonecutterSettings.xml")]
)
class StonecutterSettings : SerializablePersistentStateComponent<StonecutterSettings.Settings>(Settings()), Disposable.Default {
    companion object {
        val IT by lazy { ApplicationManager.getApplication().getService(StonecutterSettings::class.java) }
        val STATE get() = IT.state
    }

    class Settings : BaseState() {
        /**
         * Configures the behaviour of [StitcherFoldingBuilder][dev.kikugie.stonecutter.intellij.editor.StitcherFoldingBuilder].
         * - [FoldingMode.DISABLED] - no folding regions are added.
         * - [FoldingMode.LENIENT] - folding regions are added, but not automatically collapsed.
         * - [FoldingMode.AGGRESSIVE] - added folding regions are collapsed whenever the file is selected.
         */
        var foldDisabledBlocks by enum(FoldingMode.DISABLED)

        var linkDisabledBlocks by property(true)

        /**
         * Toggles the functionality of [StitcherImportOptimizer][dev.kikugie.stonecutter.intellij.editor.StitcherImportOptimizer].
         */
        var useImportOptimizer by property(true)

        /**
         * Marks files in generated Stonecutter sources as read-only.
         */
        var lockGeneratedFiles by property(true)
    }

    init {
        StonecutterCallbacks.invokeAppLoad(this)
    }
}