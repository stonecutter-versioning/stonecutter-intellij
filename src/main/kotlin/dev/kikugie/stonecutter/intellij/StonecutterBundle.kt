package dev.kikugie.stonecutter.intellij

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.function.Supplier

object StonecutterBundle {
    const val BUNDLE: @NonNls String = "messages.StonecutterBundle"
    val INSTANCE = DynamicBundle(StonecutterBundle::class.java, BUNDLE)

    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): String =
        INSTANCE.getMessage(key, *params)

    fun lazyMessage(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): Supplier<String> =
        INSTANCE.getLazyMessage(key, *params)
}