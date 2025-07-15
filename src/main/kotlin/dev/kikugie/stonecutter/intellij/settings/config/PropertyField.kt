package dev.kikugie.stonecutter.intellij.settings.config

import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

/**
 * Implements the functionality of [BeanConfigurable.BeanField][com.intellij.openapi.options.BeanConfigurable.BeanField]
 * using a Kotlin [KMutableProperty0] with the determined value type.
 *
 * Fields should be added to the [PropertyConfigurable] implementations using the [register][PropertyConfigurable.register]
 * method in the class initialiser.
 *
 * @see checkbox
 * @see dropdownSelector
 * @see buttonSelector
 */
abstract class PropertyField<T, C : JComponent>(protected val property: KMutableProperty0<T>) {
    abstract var value: T
    protected var tooltip: String = ""
    open val component: C by lazy { createComponent().apply { if (tooltip.isNotEmpty()) toolTipText = tooltip } }
    val isModified: Boolean get() = value != property()

    protected abstract fun createComponent(): C
    fun apply() { property.set(value) }
    fun reset() { value = property() }
    fun tooltip(value: String) = apply { tooltip = value }
    inline fun tooltip(builder: StringBuilder.() -> Unit) = tooltip(buildString(builder))
}