package dev.kikugie.stonecutter.intellij

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object StonecutterIcons {
    private fun icon(file: String?): Icon {
        return IconLoader.getIcon("/assets/icons/" + file, StonecutterIcons::class.java)
    }

    @JvmField val STONECUTTER: Icon = icon("stonecutter.svg")
    @JvmField val VERSION_ENTRY: Icon = icon("version.svg")
    @JvmField val VERSION_EMPTY: Icon = icon("version_empty.svg")
    @JvmField val VERSION_VCS: Icon = icon("version_vcs.svg")
}