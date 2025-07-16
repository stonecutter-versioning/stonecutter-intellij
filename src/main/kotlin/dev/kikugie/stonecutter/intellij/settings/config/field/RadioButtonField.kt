package dev.kikugie.stonecutter.intellij.settings.config.field

import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.panel
import dev.kikugie.stonecutter.intellij.settings.config.KPropertyField
import dev.kikugie.stonecutter.intellij.settings.naming.description
import dev.kikugie.stonecutter.intellij.settings.naming.display
import javax.swing.JPanel
import kotlin.enums.EnumEntries
import kotlin.reflect.KMutableProperty0

class RadioButtonField<E>(val title: String, val entries: EnumEntries<E>, property: KMutableProperty0<E>) :
    KPropertyField<E, JPanel>(property) where E : Enum<E> {
    private lateinit var buttons: MutableList<Pair<E, JBRadioButton>>

    override var value: E
        get() = buttons.first { (_, it) -> it.isSelected }.first
        set(value) = buttons.first { (it, _) -> it == value }.second.setSelected(true)

    override fun createComponent(): JPanel = panel {
        buttonsGroup(title) {
            buttons = mutableListOf()
            for (it in entries) row {
                buttons += it to radioButton(it.display(Enum<E>::name)).component.apply {
                    it.description()?.let { toolTipText = it }
                }
            }
        }
    }
}