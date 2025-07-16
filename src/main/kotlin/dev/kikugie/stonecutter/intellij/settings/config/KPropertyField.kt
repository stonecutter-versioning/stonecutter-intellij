package dev.kikugie.stonecutter.intellij.settings.config

import javax.swing.JComponent
import kotlin.reflect.KMutableProperty0

/**
 * Implements the functionality of [BeanConfigurable.BeanField][com.intellij.openapi.options.BeanConfigurable.BeanField]
 * using a Kotlin [KMutableProperty0] with the determined value type.
 *
 * In the instance lifecycle, [createComponent] is called **exactly once** to be stored for later use.
 * You can expect the [value] to be configured from the provided [property] before the component is constructed,
 * unless you mess it up yourself by referencing the [component] field.
 * **DO NOT reference the [component] field inside the [createComponent] body.**
 *
 * Fields should be added to the [PropertyConfigurable] implementations using
 * the [register][PropertyConfigurable.register] method in the class initialiser.
 *
 * When using a custom [JComponent], a new [KPropertyField] should be implemented for it.
 * For read-only entries, use [PlainField][dev.kikugie.stonecutter.intellij.settings.config.field.PlainField]
 * by constructing a panel there.
 *
 * @property property The storage for the configured value, updated only when the user submits the changes in the settings GUI.
 * For most purposes, it should be a field in [StonecutterSettings.Settings][dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.Settings],
 * referenced via [StonecutterSettings.STATE][dev.kikugie.stonecutter.intellij.settings.StonecutterSettings.STATE].
 *
 * @see checkbox
 * @see dropdownSelector
 * @see buttonSelector
 */
abstract class KPropertyField<T, C : JComponent>(protected val property: KMutableProperty0<T>) {
    abstract var value: T
    protected var tooltip: String = ""
    open val component: C by lazy { createComponent().apply { if (tooltip.isNotEmpty()) toolTipText = tooltip } }
    val isModified: Boolean get() = value != property()

    protected abstract fun createComponent(): C
    fun apply() { property.set(value) }
    fun reset() { value = property() }

    /**Configures the tooltip text for this field. If [value] is empty, no tooltip will be shown.*/
    fun tooltip(value: String) = apply { tooltip = value }

    /**Configures the tooltip text for this field. If [value] is empty, no tooltip will be shown.*/
    inline fun tooltip(builder: StringBuilder.() -> Unit) = tooltip(buildString(builder))
}