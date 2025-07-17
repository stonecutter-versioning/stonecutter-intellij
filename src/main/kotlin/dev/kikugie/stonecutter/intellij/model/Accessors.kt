@file:Suppress("NOTHING_TO_INLINE")

package dev.kikugie.stonecutter.intellij.model

import dev.kikugie.stonecutter.intellij.model.serialized.RegexReplacement
import dev.kikugie.stonecutter.intellij.model.serialized.Replacement
import dev.kikugie.stonecutter.intellij.model.serialized.StringReplacement

inline fun Sequence<Replacement>.anonymous() = filter { it.identifier == null }
inline fun Sequence<Replacement>.named() = filter { it.identifier != null }

inline fun Sequence<Replacement>.string() = filterIsInstance<StringReplacement>()
inline fun Sequence<Replacement>.regex() = filterIsInstance<RegexReplacement>()