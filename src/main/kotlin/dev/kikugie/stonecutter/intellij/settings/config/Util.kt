package dev.kikugie.stonecutter.intellij.settings.config

import dev.kikugie.stonecutter.intellij.settings.config.field.CheckboxField
import dev.kikugie.stonecutter.intellij.settings.config.field.ComboBoxField
import dev.kikugie.stonecutter.intellij.settings.config.field.PlainField
import dev.kikugie.stonecutter.intellij.settings.config.field.RadioButtonField
import java.awt.event.ActionEvent
import kotlin.enums.EnumEntries
import kotlin.reflect.KMutableProperty0

fun PropertyConfigurable.comment(text: String) =
    PlainField { row { comment(text) } }.let(::register)

fun PropertyConfigurable.link(text: String, callback: (ActionEvent) -> Unit) =
    PlainField { row { link(text) { callback(it) } } }.let(::register)

fun PropertyConfigurable.checkbox(title: String, property: KMutableProperty0<Boolean>) =
    CheckboxField(title, property).let(::register)

fun <E> PropertyConfigurable.buttonSelector(title: String, entries: EnumEntries<E>, property: KMutableProperty0<E>) where E : Enum<E> =
    RadioButtonField(title, entries, property).let(::register)

fun <E> PropertyConfigurable.dropdownSelector(title: String, entries: EnumEntries<E>, property: KMutableProperty0<E>) where E : Enum<E> =
    ComboBoxField(title, entries, property).let(::register)
