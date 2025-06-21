package dev.kikugie.stonecutter.intellij.settings

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable

class FoldingSettings : BeanConfigurable<StonecutterSettings.Companion>(StonecutterSettings, "Stonecutter"), CodeFoldingOptionsProvider {
    init {
        checkBox("Disabled blocks", StonecutterSettings::foldDisabledScopes) { StonecutterSettings.foldDisabledScopes = it }
    }
}