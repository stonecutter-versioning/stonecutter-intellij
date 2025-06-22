package dev.kikugie.stonecutter.intellij.settings.config

import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import kotlin.reflect.KMutableProperty0

fun PropertyConfigurable.checkbox(title: @NlsContexts.Checkbox String, property: KMutableProperty0<Boolean>) =
    register(CheckboxPropertyField(title, property))

fun <E : Enum<E>> PropertyConfigurable.selector(title: @NlsContexts.Checkbox String, entries: Array<E>, property: KMutableProperty0<E>) =
    register(ComboBoxPropertyField(title, entries, property))

abstract class PropertyConfigurable(
    protected var title: @NlsContexts.BorderTitle String? = null
) : UnnamedConfigurable, UiDslUnnamedConfigurable {
    protected val properties: MutableList<PropertyField<*, *>> = mutableListOf()

    fun register(property: PropertyField<*, *>) = properties.add(property)
    override fun isModified(): Boolean = properties.any(PropertyField<*, *>::isModified)
    override fun apply() = properties.forEach(PropertyField<*, *>::apply)
    override fun reset() = properties.forEach(PropertyField<*, *>::reset)
    override fun createComponent(): JComponent = panel {
        createContent()
    }

    override fun Panel.createContent() {
        if (title != null) group(title) { appendProperties() } else appendProperties()
        onApply { apply() }
        onReset { reset() }
        onIsModified { isModified }
    }

    private fun Panel.appendProperties() = properties.forEach {
        row { cell(it.component) }
    }
}

abstract class PropertyField<T, C : JComponent>(
    protected val property: KMutableProperty0<T>,
    component: C? = null
) {
    abstract var value: T
    val component: C by lazy { component ?: createComponent() }
    val isModified: Boolean get() = value != property()

    protected abstract fun createComponent(): C
    fun apply() { property.set(value) }
    fun reset() { value = property() }
}

class CheckboxPropertyField(
    val title: @NlsContexts.Checkbox String,
    property: KMutableProperty0<Boolean>,
) : PropertyField<Boolean, JCheckBox>(property) {
    override var value: Boolean
        get() = component.isSelected
        set(value) = component.setSelected(value)
    override fun createComponent(): JCheckBox = JCheckBox(title)
}

class ComboBoxPropertyField<E : Enum<E>>(
    val title: @NlsContexts.Checkbox String,
    val entries: Array<E>,
    property: KMutableProperty0<E>
) : PropertyField<E, JPanel>(property) {
    override var value: E
        get() = box.selectedItem as E
        set(value) = box.setSelectedItem(value)
    private val box by lazy { ComboBox(entries) }

    override fun createComponent(): JPanel = panel {
        row(title) { cell(box) }
    }
}