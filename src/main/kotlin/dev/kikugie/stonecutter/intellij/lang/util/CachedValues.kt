@file:Suppress("NOTHING_TO_INLINE")

package dev.kikugie.stonecutter.intellij.lang.util

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import kotlin.reflect.KProperty

interface UserDataHolderAccessor : UserDataHolder {
    operator fun <T : Any> Key<T>.getValue(ref: Any?, property: KProperty<*>): T? = getUserData(this)
    operator fun <T : Any> Key<T>.setValue(ref: Any?, property: KProperty<*>, value: T?): Unit = putUserData(this, value)
}

inline operator fun <T : Any> UserDataHolder.get(key: Key<T>): T? = getUserData(key)
inline operator fun <T : Any> UserDataHolder.set(key: Key<T>, value: T?): Unit = putUserData(key, value)