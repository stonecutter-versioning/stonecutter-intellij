package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import dev.kikugie.stonecutter.intellij.service.StonecutterCallbacks
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingMode
import dev.kikugie.stonecutter.intellij.settings.variants.FoldingStyle

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
        var foldDisabledBlocks by enum(FoldingMode.LENIENT)
        var foldedPresentation by enum(FoldingStyle.KEEP_COMMENTS)
        var linkDisabledBlocks by property(true)
        var useImportOptimizer by property(true)
        var lockGeneratedFiles by property(true)

        var refreshAfterSwitch by property(true)
        var checkedLangInject by property(false)
    }

    init {
        StonecutterCallbacks.invokeAppLoad(this)
    }
}