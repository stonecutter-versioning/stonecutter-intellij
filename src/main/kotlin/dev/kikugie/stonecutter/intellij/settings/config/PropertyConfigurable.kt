package dev.kikugie.stonecutter.intellij.settings.config

import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

abstract class PropertyConfigurable(protected var title: @NlsContexts.BorderTitle String? = null) : UnnamedConfigurable, UiDslUnnamedConfigurable {
    protected val properties: MutableList<PropertyField<*, *>> = mutableListOf()

    fun <T : PropertyField<*, *>> register(property: T): T = property.also(properties::add)
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