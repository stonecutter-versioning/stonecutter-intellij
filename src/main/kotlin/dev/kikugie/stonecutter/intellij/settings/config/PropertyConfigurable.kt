package dev.kikugie.stonecutter.intellij.settings.config

import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

/**
 * Settings category populated with [KPropertyField]s.
 *
 * Implementations should register fields in the class initializer.
 *
 * @property title The name of this category, or `null` to display it in the panel root.
 */
abstract class PropertyConfigurable(protected var title: @NlsContexts.BorderTitle String? = null) : UnnamedConfigurable, UiDslUnnamedConfigurable {
    constructor(title: String? = null, setup: PropertyConfigurable.() -> Unit) : this(title) { setup() }
    protected val properties: MutableList<KPropertyField<*, *>> = mutableListOf()

    fun <T : KPropertyField<*, *>> register(property: T): T = property.also(properties::add)
    override fun isModified(): Boolean = properties.any(KPropertyField<*, *>::isModified)
    override fun apply() = properties.forEach(KPropertyField<*, *>::apply)
    override fun reset() = properties.forEach(KPropertyField<*, *>::reset)
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