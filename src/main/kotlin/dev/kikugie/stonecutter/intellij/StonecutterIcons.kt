package dev.kikugie.stonecutter.intellij

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

private fun icon(file: String?): Icon {
    return IconLoader.getIcon("/assets/icons/$file", StonecutterIcons::class.java)
}

object StonecutterIcons {
    @JvmField val STONECUTTER: Icon = icon("stonecutter.svg")
    @JvmField val VERSION_ENTRY: Icon = icon("version.svg")
    @JvmField val VERSION_EMPTY: Icon = icon("version_empty.svg")
    @JvmField val VERSION_VCS: Icon = icon("version_vcs.svg")

    object Reference {
        @JvmField val CONSTANT: Icon = icon("reference/constant.svg")
        @JvmField val SWAP: Icon = icon("reference/swap.svg")
        @JvmField val DEPENDENCY: Icon = icon("reference/dependency.svg")
        @JvmField val REPLACEMENT: Icon = icon("reference/replacement.svg")
        @JvmField val VERSION: Icon = icon("reference/version.svg")
    }
}