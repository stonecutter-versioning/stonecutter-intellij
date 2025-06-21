package dev.kikugie.stonecutter.intellij.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SerializablePersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import dev.kikugie.stonecutter.intellij.service.StonecutterCallbacks

@Service(Service.Level.APP) @State(
    name = "dev.kikugie.stonecutter.intellij.StonecutterSettings",
    storages = [Storage("StonecutterSettings.xml")]
)
class StonecutterSettings : SerializablePersistentStateComponent<StonecutterSettings.Settings>(Settings()), Disposable.Default {
    companion object : Properties {
        private val IT get() = ApplicationManager.getApplication().getService(StonecutterSettings::class.java)
        override var foldDisabledScopes: Boolean
            get() = IT.state.foldDisabledScopes
            set(value) {
                IT.state.foldDisabledScopes = value
            }
    }

    class Settings : BaseState(), Properties {
        override var foldDisabledScopes by property(false)
    }

    interface Properties {
        var foldDisabledScopes: Boolean
    }

    init {
        StonecutterCallbacks.invokeAppLoad(this)
    }
}