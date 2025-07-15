package dev.kikugie.stonecutter.intellij.settings.config

import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

class PlainField(config: Panel.() -> Unit) : PropertyField<Unit, JPanel>(::UNIT) {
    private companion object {
        @JvmField var UNIT = Unit
    }

    override var value: Unit = UNIT
    override val component: JPanel by lazy { panel(config) }
    override fun createComponent(): JPanel = throw UnsupportedOperationException()
}