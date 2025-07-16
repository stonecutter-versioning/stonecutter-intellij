package dev.kikugie.stonecutter.intellij.settings.config.field

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.panel
import dev.kikugie.stonecutter.intellij.settings.config.KPropertyField
import javax.swing.JPanel
import kotlin.enums.EnumEntries
import kotlin.reflect.KMutableProperty0

@Suppress("UNCHECKED_CAST")
class ComboBoxField<E : Enum<E>>(val title: String, val entries: EnumEntries<E>, property: KMutableProperty0<E>) : KPropertyField<E, JPanel>(property) {
    override var value: E
        get() = box.selectedItem as E
        set(value) = box.setSelectedItem(value)
    private val box by lazy { ComboBox<E>().apply { for (it in entries) addItem(it) } }

    override fun createComponent(): JPanel = panel {
        row(title) { cell(box) }
    }
}