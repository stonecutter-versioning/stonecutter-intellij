@file:Suppress("UNCHECKED_CAST")

package dev.kikugie.stonecutter.intellij.settings.config

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel
import kotlin.enums.EnumEntries
import kotlin.reflect.KMutableProperty0

class ComboBoxField<E : Enum<E>>(val title: String, val entries: EnumEntries<E>, property: KMutableProperty0<E>) : PropertyField<E, JPanel>(property) {
    override var value: E
        get() = box.selectedItem as E
        set(value) = box.setSelectedItem(value)
    private val box by lazy { ComboBox<E>().apply { for (it in entries) addItem(it) } }

    override fun createComponent(): JPanel = panel {
        row(title) { cell(box) }
    }
}