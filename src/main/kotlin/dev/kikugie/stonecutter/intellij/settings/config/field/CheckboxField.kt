package dev.kikugie.stonecutter.intellij.settings.config.field

import dev.kikugie.stonecutter.intellij.settings.config.KPropertyField
import javax.swing.JCheckBox
import kotlin.reflect.KMutableProperty0

class CheckboxField(val title: String, property: KMutableProperty0<Boolean>) : KPropertyField<Boolean, JCheckBox>(property) {
    override var value: Boolean
        get() = component.isSelected
        set(value) = component.setSelected(value)
    override fun createComponent(): JCheckBox = JCheckBox(title)
}