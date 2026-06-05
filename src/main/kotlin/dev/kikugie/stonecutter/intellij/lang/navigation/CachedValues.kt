package dev.kikugie.stonecutter.intellij.lang.navigation

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolder
import kotlin.reflect.KProperty

interface UserDataHolderAccessor : UserDataHolder {
    operator fun <T : Any> Key<T>.getValue(ref: Any?, property: KProperty<*>): T? = getUserData(this)
    operator fun <T : Any> Key<T>.setValue(ref: Any?, property: KProperty<*>, value: T?): Unit = putUserData(this, value)
}